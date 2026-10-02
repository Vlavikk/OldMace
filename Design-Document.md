
---
# Основная идея

### Булава имеет 2 вида основной атаки:
	1. Атака на земле. Игрок бьет сущность стоя на земле)
	2. Атака в полете. Игрок бьет сущность в полете, в данном случае урон расчитываеться исходя из вертикального импульса игрока при ударе сущности

Основная способность и ценность для игрока от предмета это "Атака в полете". Оружие имеет медленную скорость атаки для компенсации способности. При нанесении урона "Атака в полете" игрок теряет всю инерцию, игрока подбрасывает чуть вверх и счетчик высоты обнуляется, игрок не получает урона от падения, сущности рядом отбрасываются назад относительно места удара, чем ближе сущность тем дальше она отбрасывается. Сущность которая получает основной урон не отбрасывается и получает урон в зависимости от накопленной высоты падения игрока

# Пакет Ресурсов (**Resource Pack**)

В качестве предмета для булавы используется Удочка с наростом(**minecraft:warped_fungus_on_a_stick**)

Для добавления текстуры Булавы используется:
**Custom Model Data: 670001**


# Техническая документация
*Все технические параметры и формулы взяты из оригинального кода Minecraft, версии 26.3*

### Основные параметры:

Основной урон: 6 (Булава добавляет атрибутом 5)

Скорость атаки: 0.6 (Булава добавляет атрибутом -3.4)

### Расчет мощности "Атака в полете":

Параметры:
```
LivingEntity attacker - атакующий
LivingEntity mob - моб, поулчающий урон
float fallDistance - высота падения атакующего
```



#### Расчет урона:
```
         double fallHeightThreshold1 = 3.0;
         double fallHeightThreshold2 = 8.0;
         double fallDistance = attacker.fallDistance;
         double damage;
         if (fallDistance <= fallHeightThreshold1) {
            damage = 4.0 * fallDistance;
         } else if (fallDistance <= fallHeightThreshold2) {
            damage = 12.0 + 2.0 * (fallDistance - fallHeightThreshold1);
         } else {
            damage = 22.0 + fallDistance - fallHeightThreshold2;
         }
         return (float)damage;
      } else {
         return 0.0F;
      }
```
##### Где:
fallHeightThreshold1 - порог высоты 1

fallHeightThreshold2 - порог высоты 2

fallDistance - высота падения атакующего

damage - урон, который добавляется к основному(Основной урон 6 + damage)

#### Расчет откидывания сущности:
```
return (3.5 - direction.length()) * 0.7F * (attacker.fallDistance > 5.0 ? 2 : 1) * (1.0 - nearby.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
```
##### Где:
direction - вектор от моба получившего урон до сущности

nearby - сущность подверженная отбрасыванию

#### При этом само отбрасывание это:
```
Vec3 direction = nearby.position().subtract(entity.position());
double knockbackPower = getKnockbackPower(attacker, nearby, direction);
Vec3 knockbackVector = direction.normalize().scale(knockbackPower);
  if (knockbackPower > 0.0) {
    nearby.push(knockbackVector.x, 0.7F, knockbackVector.z);
  }
```
##### Где:
knockbackPower - сила рассчитанная в формуле выше

knockbackVector - нормализованный(единичный) вектор direction, умноженный на knockbackPower

*При этом нужно заместить что сущность всегда подбрасывается на фиксированное расстояние вверх по координате Y (0.7)*

Сущность получает откидывание только в случае:
```
         boolean notSpectator = !nearby.isSpectator();
         boolean notPlayer = nearby != attacker && nearby != entity;
         boolean notAlliedToPlayer = !attacker.isAlliedTo(nearby);
         boolean notTamedByPlayer = !(
            nearby instanceof TamableAnimal animal && entity instanceof LivingEntity livingAttacker && animal.isTame() && animal.isOwnedBy(livingAttacker)
         );
         boolean notArmorStand = !(nearby instanceof ArmorStand armorStand && armorStand.isMarker());
         boolean withinRange = entity.distanceToSqr(nearby) <= Math.pow(3.5, 2.0);
         boolean notFlyingInCreative = !(nearby instanceof Player player && player.isCreative() && player.getAbilities().flying);

         return notSpectator && notPlayer && notAlliedToPlayer && notTamedByPlayer && notArmorStand && withinRange && notFlyingInCreative;
```

notSpectator - сущность не в режиме наблюдателя

notPlayer - сущность не является атакующим или мобом получившим урон от атаки

notAlliedToPlayer - не ебу, что то типо не является сокомандником

notArmorStand - не является стойкой для брони

withinRange - сущность в радиусе поражения (константа 3.5²)

notFlyingInCreative - сущность не в творческом режиме и не в полете


# Визуализация и звуковое сопровождение:

Атака "Атака в полете" сопровождается частицами ломания рядом стоящих блоков, так же частицами Крит. попадания и Сердечками получения урона.

#### Звуки:
При попадании по сущности с высоты более 5 блоков проигрывается звук тяжелого удара (**MACE_SMASH_GROUND_HEAVY**) иначе (**MACE_SMASH_GROUND**)
Если игрок не попал по сущности или намеренно ударил по воздуху проигрывается отдельный звук (**MACE_SMASH_AIR**)
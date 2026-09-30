package org.vlavik.oldmace.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vlavik.oldmace.Managers.MaceManager;
import org.vlavik.oldmace.OldMace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MaceCommand implements CommandExecutor, TabCompleter {

    private final MaceManager maceManager = OldMace.getMaceManager();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)){
            commandSender.sendMessage("Команда доступна только для Игроков!");
            return false;
        }
        Player player = (Player) commandSender;
        if (strings.length >= 1){
            String arg1 = strings[0];

            if (arg1.equals("getMace")){
                Player playerForGive;
                if (strings.length >= 2){
                    String playerNameForGive = strings[1];
                    Player selectedPlayer = Bukkit.getPlayer(playerNameForGive);
                    if (selectedPlayer != null) playerForGive = selectedPlayer;
                    else {
                        player.sendMessage("Игрок "+playerNameForGive+" не в сети!");
                        return false;
                    }
                }else playerForGive = player;

                boolean isSuccessful = playerGiveMace(playerForGive);
                if (isSuccessful) return true;
                else {
                    player.sendMessage("Не удалось выдать Булаву игроку!");
                    return false;
                }
            }
        }
        return false;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1){
            List<String> list = new ArrayList<>();
            list.add("getMace");
            return list;
        }else if (args.length == 2){
            if (args[0].equals("getMace")){
                List<String> list = new ArrayList<>();
                list.add("<PlayerName>");
                return list;
            }
        }
        return Collections.emptyList();
    }

    private boolean playerGiveMace(Player player){
        ItemStack maceItemStack = maceManager.createMaceItemStack();
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(maceItemStack);
        return leftover.isEmpty();
    }
}

/*
 * This file is a part of project QuickShop, the name is SubCommand_Staff.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.command.subcommand;

import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.PlayerFinder;
import org.maxgamer.quickshop.util.Util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class SubCommand_Staff implements CommandHandler<Player> {

    private final QuickShop plugin;
    private final List<String> tabCompleteList = Collections.unmodifiableList(
            Arrays.asList("add", "del", "list", "clear")
    );

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        BlockIterator bIt = new BlockIterator(sender, 10);
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop == null || (!shop.getModerator().isModerator(sender.getUniqueId()) && !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.staff"))) {
                continue;
            }
            switch (cmdArg.length) {
                case 1:
                    switch (cmdArg[0]) {
                        case "clear":
                            shop.clearStaffs();
                            plugin.text().of(sender, "shop-staff-cleared").send();
                            return;
                        case "list":
                            final List<UUID> staffs = shop.getStaffs();
                            if (staffs.isEmpty()) {
                                MsgUtil.sendDirectMessage(sender,
                                        ChatColor.GREEN
                                                + plugin.text().of(sender, "tableformat.left_begin").forLocale()
                                                + "Empty");
                                return;
                            }
                            for (UUID uuid : staffs) {
                                MsgUtil.sendDirectMessage(sender,
                                        ChatColor.GREEN
                                                + plugin.text().of(sender, "tableformat.left_begin").forLocale()
                                                + PlayerFinder.findNameByUUID(uuid));
                            }
                            return;
                        case "add":
                        case "del":
                        default:
                            plugin.text().of(sender, "command.wrong-args").send();
                            return;
                    }
                default:
                    boolean didAction = false;
                    // Loop through the arguments, skipping the action argument (0)
                    for (int i = 1; i < cmdArg.length; i++) {
                        final PlayerFinder.PlayerProfile profile = PlayerFinder.findPlayerProfileByName(cmdArg[i], false, plugin.isIncludeOfflinePlayer());
                        if (profile == null && !cmdArg[i].contains(",")) {
                            plugin.text().of(sender, "unknown-player").send();
                            continue;
                        }
                        String offlinePlayerName = profile.getName();
                        if (offlinePlayerName == null) {
                            offlinePlayerName = "null";
                        }
                        switch (cmdArg[0]) {
                            case "add":
                                shop.addStaff(profile.getUuid());
                                plugin.text().of(sender, "shop-staff-added", offlinePlayerName).send();
                                didAction = true;
                                continue;
                            case "del":
                                shop.delStaff(profile.getUuid());
                                plugin.text().of(sender,
                                        "shop-staff-deleted", offlinePlayerName).send();
                                didAction = true;
                                continue;
                            default:
                                plugin.text().of(sender, "command.wrong-args").send();
                                didAction = true;
                        }
                    }
                    if (didAction) {
                        return;
                    }
            }
        }
        //no match shop
        plugin.text().of(sender, "not-looking-at-shop").send();
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {

        if (cmdArg.length == 1) {
            return tabCompleteList;
        } else if (cmdArg.length >= 2) {
            String prefix = cmdArg[0].toLowerCase();
            if ("add".equals(prefix) || "del".equals(cmdArg[0])) {
                return Util.getPlayerList();
            }
        }
        return Collections.emptyList();
    }
}

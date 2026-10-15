package fr.rise.mods;

import fr.rise.*;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Achete bas / revend haut sur le marche du serveur. Menus et regex a adapter a votre serveur. */
public class MarketBot extends Module {
    private final Setting.Text items = add(new Setting.Text("Items (ids)", "diamond,netherite_ingot"));
    private final Setting.Num maxBuy = add(new Setting.Num("Prix achat max", 1000, 1, 100000, true));
    private final Setting.Num markup = add(new Setting.Num("Marge %", 25, 1, 200, true));
    private final Setting.Num budget = add(new Setting.Num("Budget session", 50000, 100, 1000000, true));
    private final Setting.Num scanDelay = add(new Setting.Num("Delai scan (ticks)", 80, 20, 400, true));
    private final Setting.Text openCmd = add(new Setting.Text("Commande marche", "/ah"));
    private final Setting.Text marketTitle = add(new Setting.Text("Titre menu marche", "Auction"));
    private final Setting.Text confirmTitle = add(new Setting.Text("Titre confirmation", "Confirm"));
    private final Setting.Text confirmItem = add(new Setting.Text("Item confirmer (id)", "lime_stained_glass_pane"));
    private final Setting.Text priceRegex = add(new Setting.Text("Regex prix (groupe 1)", "(?i)price[^0-9]*([0-9][0-9,.]*)"));
    private final Setting.Text relistCmd = add(new Setting.Text("Commande vente", "/ah sell {price}"));

    private final Map<String, Double> lastBuy = new HashMap<>();
    private double spent;
    private int cooldown;

    public MarketBot() { super("Market Bot", "Achete bas, revend haut sur le marche du serveur.", Category.BOTS); }

    @Override public void onEnable() { spent = 0; cooldown = 0; lastBuy.clear(); }

    @Override public void onTick() {
        if (cooldown > 0) { cooldown--; return; }
        if (spent >= budget.i()) { info("Budget de session atteint."); setEnabled(false); return; }

        Set<String> wanted = new HashSet<>();
        for (String s : items.s().split(",")) wanted.add(s.trim().toLowerCase());

        if (Compat.screen() instanceof ContainerScreen cs) {
            String title = cs.getTitle().getString();
            ChestMenu h = cs.getMenu();
            int size = h.getRowCount() * 9;

            if (title.contains(confirmTitle.s())) {
                for (int i = 0; i < size; i++)
                    if (Compat.itemId(h.getSlot(i).getItem()).equals(confirmItem.s().trim())) { click(h, i); break; }
                cooldown = 10; return;
            }
            if (title.contains(marketTitle.s())) {
                Pattern p;
                try { p = Pattern.compile(priceRegex.s()); } catch (Exception e) { info("Regex invalide."); setEnabled(false); return; }
                for (int i = 0; i < size; i++) {
                    ItemStack st = h.getSlot(i).getItem();
                    if (st.isEmpty() || !wanted.contains(Compat.itemId(st))) continue;
                    Double price = parsePrice(p, st);
                    if (price == null || price > maxBuy.i() || spent + price > budget.i()) continue;
                    click(h, i);
                    spent += price;
                    lastBuy.put(Compat.itemId(st), price);
                    info(String.format(Locale.US, "Achat %s a %.0f (depense %.0f)", Compat.itemId(st), price, spent));
                    cooldown = 10; return;
                }
                mc.player.closeContainer();
                cooldown = scanDelay.i(); return;
            }
            return;
        }

        for (int i = 0; i < 36; i++) {
            ItemStack st = Compat.stack(i);
            if (st.isEmpty() || !lastBuy.containsKey(Compat.itemId(st))) continue;
            double sell = lastBuy.remove(Compat.itemId(st)) * (1 + markup.i() / 100.0);
            Bots.toMainHand(i);
            Bots.command(relistCmd.s().replace("{price}", String.format(Locale.US, "%.0f", sell)));
            info(String.format(Locale.US, "Mise en vente %s a %.0f", Compat.itemId(st), sell));
            cooldown = 30; return;
        }
        Bots.command(openCmd.s());
        cooldown = 40;
    }

    private void click(ChestMenu h, int slot) { Compat.click(h.containerId, slot, 0, Compat.Click.PICKUP); }

    private Double parsePrice(Pattern p, ItemStack st) {
        Matcher m = p.matcher(Compat.tooltip(st));
        if (!m.find()) return null;
        try { return Double.parseDouble(m.group(1).replace(",", "")); } catch (Exception e) { return null; }
    }
}

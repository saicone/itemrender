<h1 align="center">ItemRender</h1>

<h4 align="center">Powerful easy-to-use API to modify how players see items.</h4>

<p align="center">
    <a href="https://saic.one/discord">
        <img src="https://img.shields.io/discord/974288218839191612.svg?style=flat-square&label=discord&logo=discord&logoColor=white&color=7289da"/>
    </a>
    <a href="https://www.codefactor.io/repository/github/saicone/itemrender">
        <img src="https://www.codefactor.io/repository/github/saicone/itemrender/badge?style=flat-square"/>
    </a>
    <a href="https://github.com/saicone/itemrender">
        <img src="https://img.shields.io/github/languages/code-size/saicone/itemrender?logo=github&logoColor=white&style=flat-square"/>
    </a>
    <a href="https://mvnrepository.com/artifact/com.saicone.itemrender/itemrender">
        <img src="https://img.shields.io/maven-central/v/com.saicone.itemrender/itemrender"/>
    </a>
    <a href="https://javadoc.saicone.com/itemrender/overview-summary.html">
        <img src="https://img.shields.io/badge/JavaDoc-Online-green?style=flat-square"/>
    </a>
    <a href="https://docs.saicone.com/itemrender/">
        <img src="https://img.shields.io/badge/Saicone-itemrender%20Wiki-3b3bb0?logo=github&logoColor=white&style=flat-square"/>
    </a>
</p>

Do you ever want to implement updatable items? This is the API are you looking for.

ItemRender API offers easy-to-use methods to edit client-side items.

```java
// A simple edit that parse placeholders on item name and lore
ItemRenderAPI.bukkit().register("myedit:placeholders", context -> {
    ItemStack item = context.item();
    if (item != null && item.hasItemMeta()) {
        ItemMeta meta = item.getItemMeta();
        boolean modified = false;

        String name = meta.getDisplayName();
        if (name != null) {
            name = PlaceholderAPI.setPlaceholders(context.player(), name);
            modified = true;
        }

        List<String> lore = meta.getLore();
        if (lore != null && !lore.isEmpty()) {
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, PlaceholderAPI.setPlaceholders(context.player(), lore.get(i)));
            }
            modified = true;
        }

        if (modified) {
            item.setItemMeta(meta);
            context.item(item);
        }
    }
}).when(ItemView.WINDOW, ItemView.MERCHANT);

// An edit that doesn't show player armor if the player has invisibility effect and 'invisible.armor' permission
ItemRenderAPI.bukkit().register("myedit:invisibility", context -> {
    for (Player player : Bukkit.getOnlinePlayers()) {
       if (player.getEntityId() == context.entityId()) {
           if (player.hasPotionEffect(PotionEffectType.INVISIBILITY) && player.hasPermission("invisible.armor")) {
               context.item(null);
           }
           break;
       }
    }
}).when(ItemView.EQUIPMENT);
```
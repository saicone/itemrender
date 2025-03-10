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
    <a href="https://jitpack.io/#com.saicone/itemrender">
        <img src="https://jitpack.io/v/com.saicone/itemrender.svg?style=flat-square"/>
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
ItemStack item = ...;

// Edit only item
ItemRenderAPI.register("myedit:id", item -> {
    // Edit the item...

    // If you want to hide the item at all, just return null
    return item;
}).when(ItemView.INVENTORY) // Edit client-side inventory items
  .check(item -> item != null && item.hasTag()); // Condition to apply an edit

// Edit with player
ItemRenderAPI.register("myedit:id", (player, item) -> {
    // Edit the item with player that are viewing it
    
    return item;
}).when(ItemView.THIRD_PERSON) // Edit client-side items that are rendered by other players
  .check((player, item) -> player.hasPermission("see.items.different")); // Condition to apply an edit, also compatible with player argument
```
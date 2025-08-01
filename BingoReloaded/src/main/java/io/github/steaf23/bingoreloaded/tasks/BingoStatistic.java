package io.github.steaf23.bingoreloaded.tasks;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record BingoStatistic(@NotNull Statistic stat, @Nullable EntityType entityType, @Nullable Material materialType)
{
    public enum StatisticCategory
    {
        TRAVEL,
        BLOCK_INTERACT,
        CONTAINER_INTERACT,
        DAMAGE,
        ROOT_STATISTIC,
        OTHER,
    }

    private static final Set<EntityType> validEntityTypes = getValidEntityTypes();

    public BingoStatistic(Statistic stat)
    {
        this(stat, null, null);
    }

    public BingoStatistic(Statistic stat, @Nullable EntityType entityType)
    {
        this(stat, entityType, null);
    }

    public BingoStatistic(Statistic stat, @Nullable Material materialType)
    {
        this(stat, null, materialType);
    }

    private static BingoStatistic createStatistic(@Nullable String statistic, @Nullable String entityStr, @Nullable String materialStr)
    {
        Statistic stat = Statistic.valueOf(statistic);

        EntityType entity = null;
        if (entityStr != null && !entityStr.isEmpty())
            entity = EntityType.valueOf(entityStr);

        Material material = null;
        if (materialStr != null && !materialStr.isEmpty())
            material = Material.valueOf(materialStr);

        return new BingoStatistic(stat, entity, material);
    }

    private static @Nullable EntityType getEntityFromString(@Nullable String type) {
        if (type != null && !type.isEmpty()) {
            return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE).get(new NamespacedKey("minecraft", type));
        }
        return null;
    }

    private static @Nullable Material getMaterialFromString(@Nullable String type) {
        if (type != null && !type.isEmpty()) {
            return Material.valueOf(type);
        }
        return null;
    }

    //TODO: less static?
    public static List<Statistic> getStatisticsOfCategory(StatisticCategory category)
    {
        List<Statistic> result = new ArrayList<>();
        for (var stat : Statistic.values())
        {
            if (determineStatCategory(stat) == category)
            {
                result.add(stat);
            }
        }
        return result;
    }

    private static Set<EntityType> getValidEntityTypes()
    {
        // This is the reason we cant support 1.19.2 or below, since we would have to manually add ender dragon and wither spawn eggs.
        Set<EntityType> types = new HashSet<>();
        Arrays.stream(Material.values())
                .forEach(mat -> {
                    if (mat.name().contains("_SPAWN_EGG")) {
                        types.add(EntityType.valueOf(mat.name().replace("_SPAWN_EGG", "")));
                    }
                });
        // Note: pre 1.20.5 mooshroom spawn egg needed to be parsed by hand
        return types;
    }

    public static boolean isEntityValidForStatistic(EntityType type)
    {
        return validEntityTypes.contains(type);
    }

    /**
     * @return True if this statistic is processed by the PlayerStatisticIncrementEvent
     */
    public boolean getsUpdatedWithIncrementEvent()
    {
        if (getCategory() == StatisticCategory.TRAVEL)
            return false;

        return switch (stat)
        {
            case PLAY_ONE_MINUTE,
                    SNEAK_TIME,
                    TOTAL_WORLD_TIME,
                    TIME_SINCE_REST,
                    TIME_SINCE_DEATH -> false;
            default -> true;
        };
    }

    public static StatisticCategory determineStatCategory(Statistic stat)
    {
        return switch (stat)
        {
            case DROP,
                    PICKUP,
                    USE_ITEM,
                    BREAK_ITEM,
                    CRAFT_ITEM,
                    KILL_ENTITY,
                    ENTITY_KILLED_BY,
                    MINE_BLOCK -> StatisticCategory.ROOT_STATISTIC;

            case DAMAGE_DEALT,
                    DAMAGE_TAKEN,
                    DAMAGE_DEALT_ABSORBED,
                    DAMAGE_DEALT_RESISTED,
                    DAMAGE_RESISTED,
                    DAMAGE_ABSORBED,
                    DAMAGE_BLOCKED_BY_SHIELD -> StatisticCategory.DAMAGE;

            case TALKED_TO_VILLAGER,
                    TRADED_WITH_VILLAGER,
                    DEATHS,
                    MOB_KILLS,
                    PLAYER_KILLS,
                    FISH_CAUGHT,
                    ANIMALS_BRED,
                    LEAVE_GAME,
                    JUMP,
                    DROP_COUNT,
                    PLAY_ONE_MINUTE,
                    TOTAL_WORLD_TIME,
                    SNEAK_TIME,
                    TIME_SINCE_DEATH,
                    RAID_TRIGGER,
                    ARMOR_CLEANED,
                    BANNER_CLEANED,
                    ITEM_ENCHANTED,
                    TIME_SINCE_REST,
                    RAID_WIN,
                    TARGET_HIT,
                    CLEAN_SHULKER_BOX -> StatisticCategory.OTHER;

            case CAKE_SLICES_EATEN,
                    CAULDRON_FILLED,
                    BREWINGSTAND_INTERACTION,
                    BEACON_INTERACTION,
                    NOTEBLOCK_PLAYED,
                    CAULDRON_USED,
                    NOTEBLOCK_TUNED,
                    FLOWER_POTTED,
                    RECORD_PLAYED,
                    FURNACE_INTERACTION,
                    CRAFTING_TABLE_INTERACTION,
                    SLEEP_IN_BED,
                    INTERACT_WITH_BLAST_FURNACE,
                    INTERACT_WITH_SMOKER,
                    INTERACT_WITH_LECTERN,
                    INTERACT_WITH_CAMPFIRE,
                    INTERACT_WITH_CARTOGRAPHY_TABLE,
                    INTERACT_WITH_LOOM,
                    INTERACT_WITH_STONECUTTER,
                    BELL_RING,
                    INTERACT_WITH_ANVIL,
                    INTERACT_WITH_GRINDSTONE,
                    INTERACT_WITH_SMITHING_TABLE -> StatisticCategory.BLOCK_INTERACT;

            case OPEN_BARREL,
                    CHEST_OPENED,
                    ENDERCHEST_OPENED,
                    SHULKER_BOX_OPENED,
                    TRAPPED_CHEST_TRIGGERED,
                    HOPPER_INSPECTED,
                    DROPPER_INSPECTED,
                    DISPENSER_INSPECTED -> StatisticCategory.CONTAINER_INTERACT;

			case STRIDER_ONE_CM,
				 MINECART_ONE_CM,
				 CLIMB_ONE_CM,
				 FLY_ONE_CM,
				 WALK_UNDER_WATER_ONE_CM,
				 BOAT_ONE_CM,
				 PIG_ONE_CM,
				 HORSE_ONE_CM,
				 CROUCH_ONE_CM,
				 AVIATE_ONE_CM,
				 WALK_ONE_CM,
				 WALK_ON_WATER_ONE_CM,
				 SWIM_ONE_CM,
				 FALL_ONE_CM,
				 SPRINT_ONE_CM,
                 HAPPY_GHAST_ONE_CM -> StatisticCategory.TRAVEL;
            default -> StatisticCategory.OTHER;
        };
    }

    public static String createDescription(Statistic stat)
    {
        return switch (stat)
        {
            default -> "";
        };
    }

    public static @NotNull Material getMaterial(BingoStatistic statistic)
    {
        return switch (statistic.stat)
        {
            case DAMAGE_DEALT -> Material.DIAMOND_SWORD;
            case DAMAGE_TAKEN -> Material.IRON_CHESTPLATE;
            case DEATHS -> Material.SKELETON_SKULL;
            case MOB_KILLS -> Material.CREEPER_HEAD;
            case PLAYER_KILLS -> Material.PLAYER_HEAD;
            case FISH_CAUGHT -> Material.TROPICAL_FISH;
            case ANIMALS_BRED -> Material.WHEAT;
            case LEAVE_GAME -> Material.BARRIER;
            case JUMP -> Material.RABBIT_FOOT;
            case DROP_COUNT, HOPPER_INSPECTED -> Material.HOPPER;
            case PLAY_ONE_MINUTE -> Material.CLOCK;
            case TOTAL_WORLD_TIME -> Material.FILLED_MAP;
            case WALK_ONE_CM -> Material.LEATHER_BOOTS;
            case WALK_ON_WATER_ONE_CM -> Material.ICE;
            case FALL_ONE_CM -> Material.LAVA_BUCKET;
            case SNEAK_TIME -> Material.SCULK_SHRIEKER;
            case CLIMB_ONE_CM -> Material.EMERALD_ORE;
            case FLY_ONE_CM -> Material.COMMAND_BLOCK;
            case WALK_UNDER_WATER_ONE_CM -> Material.GOLDEN_BOOTS;
            case MINECART_ONE_CM -> Material.MINECART;
            case BOAT_ONE_CM -> Material.OAK_BOAT;
            case PIG_ONE_CM -> Material.CARROT_ON_A_STICK;
            case HORSE_ONE_CM -> Material.SADDLE;
            case SPRINT_ONE_CM -> Material.FEATHER;
            case CROUCH_ONE_CM -> Material.SCULK_SENSOR;
            case AVIATE_ONE_CM -> Material.ELYTRA;
            case TIME_SINCE_DEATH -> Material.RECOVERY_COMPASS;
            case TALKED_TO_VILLAGER -> Material.POPPY;
            case TRADED_WITH_VILLAGER -> Material.EMERALD;
            case CAKE_SLICES_EATEN -> Material.CAKE;
            case CAULDRON_FILLED -> Material.CAULDRON;
            case CAULDRON_USED -> Material.WATER_BUCKET;
            case ARMOR_CLEANED -> Material.LEATHER_CHESTPLATE;
            case BANNER_CLEANED -> Material.WHITE_BANNER;
            case BREWINGSTAND_INTERACTION -> Material.BREWING_STAND;
            case BEACON_INTERACTION -> Material.BEACON;
            case DROPPER_INSPECTED -> Material.DROPPER;
            case DISPENSER_INSPECTED -> Material.DISPENSER;
            case NOTEBLOCK_PLAYED, NOTEBLOCK_TUNED -> Material.NOTE_BLOCK;
            case FLOWER_POTTED -> Material.FLOWER_POT;
            case TRAPPED_CHEST_TRIGGERED -> Material.TRAPPED_CHEST;
            case ENDERCHEST_OPENED -> Material.ENDER_CHEST;
            case ITEM_ENCHANTED -> Material.ENCHANTING_TABLE;
            case RECORD_PLAYED -> Material.MUSIC_DISC_CAT;
            case FURNACE_INTERACTION -> Material.FURNACE;
            case CRAFTING_TABLE_INTERACTION -> Material.CRAFTING_TABLE;
            case CHEST_OPENED -> Material.CHEST;
            case SLEEP_IN_BED -> Material.RED_BED;
            case SHULKER_BOX_OPENED -> Material.SHULKER_BOX;
            case TIME_SINCE_REST -> Material.YELLOW_BED;
            case SWIM_ONE_CM -> Material.BUBBLE_CORAL;
            case DAMAGE_DEALT_ABSORBED -> Material.DAMAGED_ANVIL;
            case DAMAGE_DEALT_RESISTED -> Material.NETHERITE_SWORD;
            case DAMAGE_BLOCKED_BY_SHIELD -> Material.SHIELD;
            case DAMAGE_ABSORBED -> Material.SPONGE;
            case DAMAGE_RESISTED -> Material.DIAMOND_CHESTPLATE;
            case CLEAN_SHULKER_BOX -> Material.SHULKER_SHELL;
            case OPEN_BARREL -> Material.BARREL;
            case INTERACT_WITH_BLAST_FURNACE -> Material.BLAST_FURNACE;
            case INTERACT_WITH_SMOKER -> Material.SMOKER;
            case INTERACT_WITH_LECTERN -> Material.LECTERN;
            case INTERACT_WITH_CAMPFIRE -> Material.CAMPFIRE;
            case INTERACT_WITH_CARTOGRAPHY_TABLE -> Material.CARTOGRAPHY_TABLE;
            case INTERACT_WITH_LOOM -> Material.LOOM;
            case INTERACT_WITH_STONECUTTER -> Material.STONECUTTER;
            case BELL_RING -> Material.BELL;
            case RAID_TRIGGER -> Material.CROSSBOW;
            case RAID_WIN -> Material.TOTEM_OF_UNDYING;
            case INTERACT_WITH_ANVIL -> Material.ANVIL;
            case INTERACT_WITH_GRINDSTONE -> Material.GRINDSTONE;
            case TARGET_HIT -> Material.TARGET;
            case INTERACT_WITH_SMITHING_TABLE -> Material.SMITHING_TABLE;
            case STRIDER_ONE_CM -> Material.WARPED_FUNGUS_ON_A_STICK;
            case HAPPY_GHAST_ONE_CM -> Material.HAPPY_GHAST_SPAWN_EGG;
            case DROP,
                    PICKUP,
                    MINE_BLOCK,
                    USE_ITEM,
                    BREAK_ITEM,
                    CRAFT_ITEM,
                    KILL_ENTITY,
                    ENTITY_KILLED_BY -> rootStatMaterial(statistic);
            default -> Material.BEDROCK;
        };
    }

    public StatisticCategory getCategory()
    {
        return determineStatCategory(stat);
    }

    private static Material rootStatMaterial(BingoStatistic statistic)
    {
        if (statistic.materialType != null &&
                (statistic.stat.getType() == Statistic.Type.ITEM || statistic.stat.getType() == Statistic.Type.BLOCK))
        {
            return statistic.materialType;
        }
        else if (statistic.entityType != null &&
                statistic.stat.getType() == Statistic.Type.ENTITY)
        {
            return Material.valueOf(statistic.entityType.name() + "_SPAWN_EGG");
        }

        return Material.GLOBE_BANNER_PATTERN;
    }

    public boolean hasMaterialComponent()
    {
        return materialType != null;
    }

    public boolean hasEntityComponent()
    {
        return entityType != null;
    }
}

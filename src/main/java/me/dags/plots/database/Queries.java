package me.dags.plots.database;

import me.dags.commandbus.Format;
import me.dags.plots.database.statment.Delete;
import me.dags.plots.database.statment.Insert;
import me.dags.plots.database.statment.Select;
import me.dags.plots.database.statment.Where;
import me.dags.plots.plot.PlotId;
import me.dags.plots.plot.PlotMeta;
import me.dags.plots.plot.PlotUser;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Queries {

    public static Where.Builder matchUser(UUID uuid) {
        return Where.of(Keys.USER_ID, "=", uuid.toString());
    }

    public static Where.Builder matchWhitelisted(PlotId plotId) {
        return Where.of(Keys.PLOT_ID, "=", plotId.toString());
    }

    public static Where.Builder matchPlotOwners(PlotId plotId) {
        return matchPlot(plotId)
                .and(Keys.META_OWNER, "=", true);
    }

    public static Where.Builder matchPlot(PlotId plotId) {
        return Where.of(Keys.PLOT_ID, "=", plotId.toString());
    }

    public static Where.Builder matchCustomName(UUID uuid, String name) {
        return Where.of(Keys.USER_ID, "=", uuid.toString(), true)
                .and(Keys.META_NAME, "=", name);
    }

    public static ResultTransformer<Boolean> claimed() {
        return result -> {
            if (result.next()) {
                return result.getString(Keys.USER_ID) != null;
            }
            return false;
        };
    }

    public static ResultTransformer<Optional<User>> owner() {
        return result -> {
            if (result.next()) {
                String id = result.getString(Keys.USER_ID);
                if (id != null) {
                    UUID uuid = UUID.fromString(id);
                    return Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(uuid);
                }
            }
            return Optional.empty();
        };
    }

    public static ResultTransformer<Set<UUID>> userIds() {
        return result -> {
            Set<UUID> set = new HashSet<>();
            while (result.next()) {
                String id = result.getString(Keys.USER_ID);
                if (id != null) {
                    set.add(UUID.fromString(id));
                }
            }
            return set;
        };
    }

    public static ResultTransformer<PlotId> plotTransformer() {
        return result -> {
            String id = null;
            if (result.next()) {
                id = result.getString(Keys.PLOT_ID);
            }
            return PlotId.valueOf(id);
        };
    }

    public static ResultTransformer<Text> plotInfo(Format format) {
        return result -> {
            Format.MessageBuilder owner = format.message();
            Format.MessageBuilder whitelist = format.message().append(Text.NEW_LINE).info("Whitelisted:");
            final UserStorageService service = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
            while (result.next()) {
                String id = result.getString(Keys.USER_ID);
                if (id != null) {
                    UUID uuid = UUID.fromString(id);
                    Optional<User> user = service.get(uuid);
                    if (user.isPresent()) {
                        if (result.getBoolean(Keys.META_OWNER)) {
                            owner.append(Text.NEW_LINE).info("Owner: ").stress(user.get().getName());
                        } else {
                            whitelist.append(Text.NEW_LINE).info(" - ").stress(user.get().getName());
                        }
                    }
                }
            }
            return owner.append(whitelist.build()).build();
        };
    }

    public static ResultTransformer<PlotUser> userTransformer(PlotUser.Builder builder) {
        return result -> {
            while (result.next()) {
                String id = result.getString(Keys.PLOT_ID);
                if (id != null) {
                    PlotId plotId = PlotId.valueOf(id);
                    PlotMeta meta = PlotMeta.EMPTY;
                    String name = result.getString(Keys.META_NAME);
                    Boolean owner = result.getBoolean(Keys.META_OWNER);
                    meta = PlotMeta.builder().name(name).owner(owner).build();
                    builder.plot(plotId, meta);
                }
            }
            return builder.build();
        };
    }

    public static Select.Builder<Text> selectPlotInfo(String world, PlotId plotId, Format format) {
        return new Select.Builder<Text>()
                .select("*")
                .from(world)
                .where(matchPlot(plotId).build())
                .transformer(plotInfo(format));
    }

    public static Select.Builder<Boolean> isClaimed(String world, PlotId plotId) {
        return new Select.Builder<Boolean>()
                .select(Keys.USER_ID)
                .from(world)
                .where(matchPlot(plotId).build())
                .transformer(claimed());
    }

    public static Select.Builder<PlotUser> selectUser(String world, UUID uuid) {
        return new Select.Builder<PlotUser>()
                .select("*")
                .from(world)
                .where(matchUser(uuid).build())
                .transformer(userTransformer(PlotUser.builder().world(world).uuid(uuid)));
    }

    public static Select.Builder<Optional<User>> selectPlotOwner(String world, PlotId plotId) {
        return new Select.Builder<Optional<User>>()
                .select(Keys.USER_ID)
                .from(world)
                .where(matchPlot(plotId).build())
                .transformer(owner());
    }

    public static Select.Builder<Set<UUID>> selectWhitelistedUsers(String world, PlotId plotId) {
        return new Select.Builder<Set<UUID>>()
                .select(Keys.USER_ID)
                .from(world)
                .where(matchWhitelisted(plotId).build())
                .transformer(userIds());
    }

    public static Select.Builder<PlotId> selectPlotByName(UUID owner, String world, String name) {
        return new Select.Builder<PlotId>()
                .select(Keys.PLOT_ID)
                .from(world)
                .where(matchCustomName(owner, name).build())
                .transformer(plotTransformer());
    }

    public static Insert.Builder updateUserPlot(PlotUser user, PlotId plotId, PlotMeta meta) {
        Insert.Builder builder = new Insert.Builder()
                .in(user.getWorld())
                .update(Keys.UID, Keys.uid(user.getUUID(), plotId))
                .set(Keys.USER_ID, user.getUUID().toString())
                .set(Keys.PLOT_ID, plotId.toString());

        if (meta.hasMeta()) {
            builder.set(Keys.META_NAME, meta.getName());
            builder.set(Keys.META_OWNER, meta.isOwner());
        }
        return builder;
    }

    public static Delete.Builder deleteWhitelisted(PlotUser user, PlotId plotId) {
        return new Delete.Builder()
                .in(user.getWorld())
                .where(Where.of(Keys.UID, "=", Keys.uid(user.getUUID(), plotId))
                        .and(Where.of(Keys.META_OWNER, "=", "NULL").or(Keys.META_OWNER, "=", false)).build());
    }

    public static Delete.Builder deleteUserPlot(PlotUser user, PlotId plotId) {
        return new Delete.Builder()
                .in(user.getWorld())
                .where(Where.of(Keys.UID, "=", Keys.uid(user.getUUID(), plotId)).build());
    }

    public static Delete.Builder deletePlot(String world, PlotId plotId) {
        return new Delete.Builder()
                .in(world)
                .where(matchPlot(plotId).build());
    }
}

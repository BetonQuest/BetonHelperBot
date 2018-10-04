/**
 * BetonHelperBot
 * Copyright (C) 2018 Jonas Blocher
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.ungefroren.discord.BetonHelper;

import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.ungefroren.discord.BetonHelper.utils.FileHelper;
import de.ungefroren.discord.BetonHelper.wiki.BetonWiki;
import de.ungefroren.discord.BetonHelper.wiki.Tip;
import net.dv8tion.jda.client.events.relationship.FriendRequestReceivedEvent;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main file of the bot
 * <p>
 * Created on 24.09.2018.
 *
 * @author Jonas Blocher
 */
public class BetonHelperBot {

    public final static Logger log = LoggerFactory.getLogger(BetonHelperBot.class);
    private static final int WIKI_SYNCH_INTERVALL = 15;//In mintues
    private static BetonHelperBot instance;
    private JDA jda;
    private BetonWiki wiki;
    private ScheduledExecutorService executorService;

    public BetonHelperBot() {
        instance = this;
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(loadToken()).build();
            jda.awaitReady();
        } catch (LoginException e) {
            log.error("Error while login: " + e.getMessage());
            System.exit(1);
            return;
        } catch (InterruptedException ignored) {
            return;
        }
        onStart();
        Runtime.getRuntime().addShutdownHook(new Thread(this::onStop));
        scheduleRestart();

    }

    public static BetonHelperBot getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        new BetonHelperBot();
    }

    private void onStart() {
        log.info("BetonHelperBot succesfully logged in!");
        wiki = new BetonWiki();
        wiki.synchronizeWiki();
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(wiki::synchronizeWiki, WIKI_SYNCH_INTERVALL, WIKI_SYNCH_INTERVALL, TimeUnit.MINUTES);
        jda.setEventManager(new AnnotatedEventManager());
        jda.addEventListener(this);
        jda.getPresence().setGame(Game.watching("Mention me if you need help!"));
    }

    private void onStop() {
        log.info("BetonHelperBot shutting down...");
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
            log.info("Scheduler terminated!");
        } catch (InterruptedException e) {
            log.error("", e);
        }
        log.info(" ");
        log.info("Shudown completed!");
        log.info(" ");
        log.info(" ");
    }

    @SubscribeEvent
    public void onMessage(MessageReceivedEvent event) {
        if ((event.getChannelType() == ChannelType.PRIVATE && !isSelfUser(event.getAuthor()))
                || event.getMessage().getMentionedUsers().stream().anyMatch(this::isSelfUser)) {
            Tip tip = wiki.findTip(event.getMessage().getContentDisplay());
            if (tip == null) {
                log.warn("Could not find tip for the following message: " + event.getMessage());
                return;
            }
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(new Color(198, 140, 83))
                    .setAuthor(tip.getTitle())
                    .setDescription(tip.getText())
                    .setFooter("from BetonQuest wiki",
                               "https://cdn.discordapp.com/app-icons/494162764403572748/d7680ea0dfa2d1da1a95676435f526c1.png?size=32");
            tip.getAdditionalInformation().forEach(info -> embed.addField(info.getTitle(), info.getContent(), false));
            if (wiki.getSynchronized_timestamp() != null) embed.setTimestamp(wiki.getSynchronized_timestamp());
            try {
                event.getChannel().sendMessage(event.getAuthor().getAsMention()).embed(embed.build()).queue();
            } catch (InsufficientPermissionException ignored) {
            }
        }
    }

    @SubscribeEvent
    public void acceptFriendRequests(FriendRequestReceivedEvent event) {
        event.getFriendRequest().accept();
    }

    /**
     * Schedules the bot to shutdown at 4:20 (handle restart with simple loop in start script)
     */
    private void scheduleRestart() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime restartTime = now.withHour(4).withMinute(20).withSecond(0);
        if (now.compareTo(restartTime) > 0) restartTime = restartTime.plusDays(1);
        executorService.schedule(() -> {
            System.out.println();
            System.out.println("Restarting...");
            System.out.println();
            System.exit(0);
        }, Duration.between(now, restartTime).getSeconds(), TimeUnit.SECONDS);
    }

    /**
     * Loads the authentication token for the bot from file
     *
     * @return the authentication token
     */
    private String loadToken() {
        try {
            File tokenFile = new File("AUTH_TOKEN.txt");
            if (!tokenFile.exists()) {
                tokenFile.createNewFile();
                log.error("Please specify a vaild authentication token for the bot!");
                System.exit(1);
                return null;
            }
            String token = FileHelper.readToString(tokenFile);
            if (token == null) {
                log.error("Could not load the the authentication token!");
                System.exit(1);
                return null;
            }
            return token.replace("\n", "").replace("\r", "");
        } catch (IOException e) {
            log.error("Error while loading the authentication token:", e);
            System.exit(1);
            return null;
        }
    }

    /**
     * @param user a discord user
     * @return if the supplied discord user account is this bot account himself
     */
    public boolean isSelfUser(User user) {
        return user.getIdLong() == jda.getSelfUser().getIdLong();
    }

    /**
     * @return the name of the bot account
     */
    public String getSelfUserName() {
        return jda.getSelfUser().getName();
    }
}

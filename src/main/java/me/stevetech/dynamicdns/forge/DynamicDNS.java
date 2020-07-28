package me.stevetech.dynamicdns.forge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

@Mod(modid = DynamicDNS.MODID,
        version = DynamicDNS.VERSION,
        acceptableRemoteVersions = "*")
public class DynamicDNS {
    public static final String MODID = "DynamicDNS";
    public static final String VERSION = "1.0";
    private static Configuration config;
    private static int[] configInt;
    private static String[] configString;

    private static final String messagePrefix = EnumChatFormatting.GOLD + "[DynamicDNS] " + EnumChatFormatting.RESET;

    @EventHandler
    public static void preinit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        syncConfig();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println(MODID + " " + VERSION + " is now up and running!");

        FMLCommonHandler.instance().bus().register(new TickEventHandler());
        MinecraftForge.EVENT_BUS.register(new TickEventHandler());
    }

    @EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        updateDuckDNS(configString[0], configString[1], "");
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandHandle()); // MyCustomCommand must inherit the CommandBase class
    }

    public static class TickEventHandler {
        private long tickCount;

        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event) {
            tickCount++;
            if (tickCount % (configInt[0]*40) == 0) {
                updateDuckDNS(configString[0], configString[1], "");
            }
        }
    }

    public class CommandHandle extends CommandBase{
        @Override
        public String getCommandName() {
            return "updateip";
        }

        @Override
        public String getCommandUsage(ICommandSender iCommandSender) {
            return "/UpdateIP [ip]";
        }

        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            if (args.length == 1) {
                if (updateDuckDNS(configString[0], configString[1], args[0])) {
                    sender.addChatMessage(new ChatComponentText(messagePrefix + "Updated IP."));
                } else {
                    sender.addChatMessage(new ChatComponentText(messagePrefix + "Error updating IP, check the log for details."));
                }
            } else {
                if (updateDuckDNS(configString[0], configString[1], "")) {
                    sender.addChatMessage(new ChatComponentText(messagePrefix + "Updated IP."));
                } else {
                    sender.addChatMessage(new ChatComponentText(messagePrefix + "Error updating IP, check the log for details."));
                }
            }
        }
    }


    public static void syncConfig() { // Gets called from preInit
        try {
            // Load config
            config.load();

            // Read props from config
            Property configPeriod = config.get(Configuration.CATEGORY_GENERAL,
                    "period",
                    "3600");
            Property configDomain = config.get(Configuration.CATEGORY_GENERAL,
                    "domain",
                    "exampledomain");
            Property configToken = config.get(Configuration.CATEGORY_GENERAL,
                    "token",
                    "a7c4d0ad-114e-40ef-ba1d-d217904a50f2");

            configInt = new int[]{configPeriod.getInt()};
            configString = new String[]{configDomain.getString(), configToken.getString()};

        } catch (Exception e) {
            // Failed reading/writing, just continue
        } finally {
            // Save props to config IF config changed
            if (config.hasChanged()) config.save();
        }
    }

    public static boolean updateDuckDNS(String domain, String token, String ip) {
        try {
            URL url = new URL("https://www.duckdns.org/update?domains=" + domain + "&token=" + token + "&ip=" + ip);
            URLConnection conn = url.openConnection();
            conn.connect();
            String data = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
            if (data.equals("OK")) {
                System.out.println("Updated IP on DuckDNS");
                return true;
            } else if (data.equals("KO")) {
                System.out.println("Error updating IP on DuckDNS: Check your Domain and Token are correct in the config");
            } else {
                System.out.println("Error updating IP on DuckDNS");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    return false;
    }
}


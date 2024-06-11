package com.autologin;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Objects;


public class Event implements ClientReceiveMessageEvents.Game, ClientSendMessageEvents.Command {

    ConfigA config=new ConfigA();
    @Override
    public void onReceiveGameMessage(Text message, boolean overlay) {
        if(MinecraftClient.getInstance().player.networkHandler.getServerInfo()!=null){
            String Ip = MinecraftClient.getInstance().player.networkHandler.getServerInfo().address;
            if (message.toString().contains("/reg") && Objects.equals(config.getString("AutoLogin", "autoRegister"), "true")) {
                String password = config.getString("AutoLogin", "autoRegisterPassword");
                if (MinecraftClient.getInstance().player != null) {
                    if(countBrackets(message.getString())==2) {
                        MinecraftClient.getInstance().player.networkHandler.sendCommand("register " + password + " " + password);
                    }else{
                        MinecraftClient.getInstance().player.networkHandler.sendCommand("register " + password);
                    }
                    if (Ip != null) {
                        config.addString("Data", Ip, password);
                    }
                }
            } else if (message.toString().contains("/log") && Objects.equals(config.getString("AutoLogin", "autoLogin"), "true")) {
                String password = config.getString("Data", Ip);
                if (password != null) {
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.networkHandler.sendCommand("login " + password);
                    }
                }
            }
        }
    }
    @Override
    public void onSendCommandMessage(String command) {
        if(command.contains("log")){
            String[] parts = command.split(" ");
            String password = parts[1];
            config.addString("Data",MinecraftClient.getInstance().player.networkHandler.getServerInfo().address,password);
        }
    }
    public int countBrackets(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '{' || ch == '(' || ch == '[' || ch == '<') {
                count++;
            }
        }
        return count;
    }
}



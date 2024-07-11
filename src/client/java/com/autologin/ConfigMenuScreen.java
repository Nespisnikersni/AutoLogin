package com.autologin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Environment(EnvType.CLIENT)
public class ConfigMenuScreen extends Screen {

    private ScheduledExecutorService executorService;
    public void displayTextForSeconds(String text, TextWidget textField, int seconds) {
        textField.setMessage(Text.of(text));
        executorService.schedule(() -> {
            textField.setMessage(Text.of(""));
        }, seconds, TimeUnit.SECONDS);
    }

    protected ConfigMenuScreen() {
        super(Text.literal("My tutorial screen"));
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    private ConfigA config=new ConfigA();
    public ButtonWidget button2;
    public ButtonWidget button3;
    public ButtonWidget button4;
    public TextWidget buttonT2;
    public TextWidget buttonT3;
    public TextWidget buttonT4;
    public TextWidget buttonT5;
    public TextWidget buttonT6;
    public TextFieldWidget inputField;
    private boolean autoLogin = Boolean.valueOf(config.getValue("AutoLogin","autoLogin"));
    private boolean autoRegister = Boolean.valueOf(config.getValue("AutoLogin","autoRegister"));
    @Override
    protected void init() {
        button2 = ButtonWidget.builder(Text.literal(String.valueOf(autoLogin)), button -> {
                    autoLogin = !autoLogin;
                    if(autoLogin){
                        config.setValue("AutoLogin","autoLogin","true");
                        button2.setMessage(Text.of("§a"+String.valueOf(autoLogin)));
                    }else {
                        config.setValue("AutoLogin","autoLogin","false");
                        button2.setMessage(Text.of("§4"+String.valueOf(autoLogin)));
                    }
                })
                .dimensions(width / 2, 100, 70, 20)
                .tooltip(Tooltip.of(Text.of("§atrue §for §4false")))
                .build();
        if(button2.getMessage()!=Text.literal("§atrue")&&autoLogin){
            button2.setMessage(Text.literal("§atrue"));
        }else if(button2.getMessage()!=Text.literal("§4false")&&!autoLogin){
            button2.setMessage(Text.literal("§4false"));
        }
        button4 = ButtonWidget.builder(Text.literal("Exit"), button -> {this.close();})
                .dimensions(width-45, 5, 40, 20)
                .tooltip(Tooltip.of(Text.of("click to exit")))
                .build();
        button3 = ButtonWidget.builder(Text.literal(String.valueOf(autoRegister)), button -> {
                    autoRegister = !autoRegister;
                    if(autoRegister){
                        config.setValue("AutoLogin","autoRegister","true");
                        button3.setMessage(Text.of("§atrue"));
                    }else {
                        config.setValue("AutoLogin","autoRegister","false");
                        button3.setMessage(Text.of("§4false"));
                    }
                })
                .dimensions(width / 2, 120, 70, 20)
                .tooltip(Tooltip.of(Text.of("§atrue §for §4false")))
                .build();
        buttonT2 = new TextWidget(width / 2 - 75, 100, 100, 20, Text.literal("autologin") , textRenderer);
        buttonT3 = new TextWidget(width / 2 - 80, 120, 100, 20, Text.literal("autoregister") , textRenderer);
        buttonT4 = new TextWidget(width / 2 - 40, 0, 100, 20, Text.literal("AutoLoginSettings") , textRenderer);
        buttonT5 = new TextWidget(width / 2 - 120, 140, 120, 20, Text.literal("autoregisterpassword") , textRenderer);
        buttonT6 = new TextWidget(width / 2, 161, 100, 20, Text.literal("") , textRenderer);
        inputField = new TextFieldWidget(textRenderer, width / 2, 141, 70, 20, Text.of("password"));
        if(config.getValue("AutoLogin", "autoRegisterPassword")!=config.encryptPassword(inputField.getText())){
            inputField.setText(ConfigA.decryptString(config.getValue("AutoLogin", "autoRegisterPassword")));
        }
        if(button3.getMessage()!=Text.literal("§atrue")&&autoRegister){
            button3.setMessage(Text.literal("§atrue"));
        }else if(button3.getMessage()!=Text.literal("§4false")&&!autoRegister){
            button3.setMessage(Text.literal("§4false"));
        }
        addDrawableChild(inputField);
        addDrawableChild(buttonT2);
        addDrawableChild(buttonT3);
        addDrawableChild(buttonT4);
        addDrawableChild(buttonT5);
        addDrawableChild(buttonT6);
        addDrawableChild(button2);
        addDrawableChild(button3);
        addDrawableChild(button4);
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER||keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            String enteredValue = inputField.getText();
            config.setValue("AutoLogin", "autoRegisterPassword", config.encryptPassword(enteredValue));
            displayTextForSeconds("§aPassword added",buttonT6,1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

}


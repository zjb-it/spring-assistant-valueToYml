package com.github.zjb.setting;

import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@State(
        name = "org.intellij.sdk.settings.AppSettingsState",
        storages = @Storage("ValueToYmlSettingsPlugin.xml")
)
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {


    public static AppSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(AppSettingsState.class);
    }

    public List<Object> ANNOTATIONS = Lists.newArrayList("org.apache.rocketmq.spring.annotation.RocketMQMessageListener", "org.springframework.kafka.annotation.KafkaListener", "org.springframework.cloud.openfeign.FeignClient", "org.springframework.beans.factory.annotation.Value");


    @Nullable
    @Override
    public AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
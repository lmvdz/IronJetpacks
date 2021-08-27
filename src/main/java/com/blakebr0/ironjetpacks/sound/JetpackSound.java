package com.blakebr0.ironjetpacks.sound;

import com.blakebr0.ironjetpacks.util.JetpackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class JetpackSound extends MovingSoundInstance {
    private static final Map<Integer, JetpackSound> PLAYING_FOR = Collections.synchronizedMap(new HashMap<>());
    private final PlayerEntity player;
    
    public JetpackSound(PlayerEntity player) {
        super(ModSounds.JETPACK.get(), SoundCategory.PLAYERS);
        this.player = player;
        this.repeat = true;
        PLAYING_FOR.put(player.getId(), this);
    }
    
    public static boolean playing(int entityId) {
        return PLAYING_FOR.containsKey(entityId) && PLAYING_FOR.get(entityId) != null && !PLAYING_FOR.get(entityId).isDone();
    }
    
    @Override
    public void tick() {
        BlockPos pos = this.player.getBlockPos();
        this.x = (float) pos.getX();
        this.y = (float) pos.getY() - 10;
        this.z = (float) pos.getZ();
        
        if (!JetpackUtils.isFlying(this.player)) {
            synchronized (PLAYING_FOR) {
                PLAYING_FOR.remove(this.player.getId());
                setDone();
            }
        }
    }
}

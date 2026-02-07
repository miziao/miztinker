package com.mizi.miztinker.client;

import com.mizi.miztinker.entity.boss.BossEntity;
import com.mizi.miztinker.entity.boss.BossMusic;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

import java.util.WeakHashMap;

public class ClientBossMusicHandler {
    private static final WeakHashMap<BossEntity, BossMusic> bossMusics = new WeakHashMap<>();

    public static void tick(BossEntity boss) {
        Minecraft mc = Minecraft.getInstance();
        BossMusic music = bossMusics.get(boss);

        float musicVolume = mc.options.getSoundSourceVolume(SoundSource.MUSIC);

        // 条件1：Boss存活且音乐未静音
        if (boss.isAlive() && musicVolume > 0.0F) {
            // 条件2：音乐未开始或需要重新创建实例
            if (music == null || music.isStopped()) {
                music = BossMusic.create(boss);
                if (music != null) {
                    bossMusics.put(boss, music);
                }
            }

            // 安全播放检查（包括null检查）
            if (music != null && !mc.getSoundManager().isActive(music)) {
                mc.getSoundManager().play(music);
            }
        }
        // Boss死亡或音乐静音时的清理逻辑
        else if (music != null) {
            mc.getSoundManager().stop(music);
            bossMusics.remove(boss);
        }

        // 更新音乐位置和状态 (BossMusic本身是TickableSoundInstance，主要是更新音量等)
        if (music != null && !music.isStopped()) {
            // SoundManager会tick它，但我们可能需要手动更新一些依赖于boss位置的逻辑，
            // 不过原BossMusic.tick()看起来已经包含了自己的逻辑。
            // 这里主要是确保能在WeakHashMap中持有引用。
        }
    }
}

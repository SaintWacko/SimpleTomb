package com.lothrazar.simpletomb.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

public class ParticleDataTwoInt implements ParticleOptions {

  @SuppressWarnings("deprecation")
  public static final Deserializer<ParticleDataTwoInt> DESERIALIZER = new Deserializer<ParticleDataTwoInt>() {

    @Override
    public ParticleDataTwoInt fromCommand(ParticleType<ParticleDataTwoInt> particleType, StringReader reader) throws CommandSyntaxException {
      if (reader.canRead()) {
        reader.expect(' ');
      }
      int oneInt = 0xffffff, twoInt = 0xffffff;
      if (reader.canRead()) {
        oneInt = reader.readInt();
      }
      if (reader.canRead()) {
        reader.expect(' ');
      }
      if (reader.canRead()) {
        twoInt = reader.readInt();
      }
      return new ParticleDataTwoInt(particleType, oneInt, twoInt);
    }

    @Override
    public ParticleDataTwoInt fromNetwork(ParticleType<ParticleDataTwoInt> particleType, FriendlyByteBuf buf) {
      return new ParticleDataTwoInt(particleType, buf.readInt(), buf.readInt());
    }
  };
  private final ParticleType<ParticleDataTwoInt> particleType;
  public int oneInt, twoInt;

  public ParticleDataTwoInt(ParticleType<ParticleDataTwoInt> particleType, int oneInt, int twoInt) {
    this.particleType = particleType;
    this.oneInt = oneInt;
    this.twoInt = twoInt;
  }

  @Override
  public ParticleType<?> getType() {
    return this.particleType;
  }

  @Override
  public void writeToNetwork(FriendlyByteBuf buf) {
    buf.writeInt(this.oneInt);
    buf.writeInt(this.twoInt);
  }

  @Override
  public String writeToString() {
    return String.format(Locale.ROOT, "%s %d %d", ForgeRegistries.PARTICLE_TYPES.getKey(getType()), this.oneInt, this.twoInt);
  }
}

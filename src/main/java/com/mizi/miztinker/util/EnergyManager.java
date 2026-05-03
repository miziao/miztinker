package com.mizi.miztinker.util;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class EnergyManager {
    private static final List<IEnergyCompat> energyCompats = new CopyOnWriteArrayList<>();

    static {
        registerEnergyCompat(new DraconicNetworkCompat());
        registerEnergyCompat(new MekanismCompat());
        registerEnergyCompat(new IndustrialForegoingCompat());
        registerEnergyCompat(new ThermalUnlimitedCompat());
        registerEnergyCompat(new UniversalReflectionCompat());
        registerEnergyCompat(new ForgeEnergyCompat());
    }

    public static void registerEnergyCompat(IEnergyCompat compat) {
        if (compat != null && !energyCompats.contains(compat)) {
            energyCompats.add(compat);
        }
    }

    public static void chargeEnergy(@NotNull ItemStack stack, long amount) {
        if (stack.isEmpty()) return;
        for (IEnergyCompat compat : energyCompats) {
            if (compat.isUsable() && compat.hasEnergyCapability(stack, null)) {
                if (compat.receiveEnergy(stack, amount, false, null)) return;
            }
        }
    }

    public static boolean fillEnergyCompletely(BlockEntity be) {
        if (be == null || be.isRemoved()) return false;
        boolean filled = attemptFill(be, null);
        for (Direction dir : Direction.values()) {
            if (attemptFill(be, dir)) filled = true;
        }
        return filled;
    }

    private static boolean attemptFill(BlockEntity be, @Nullable Direction side) {
        for (IEnergyCompat compat : energyCompats) {
            if (compat.isUsable() && compat.hasEnergyCapability(be, side)) {
                long max = compat.getMaxEnergyStored(be, side);
                if (compat.receiveEnergy(be, max, false, side)) return true;
            }
        }
        return false;
    }

    public interface IEnergyCompat {
        boolean isUsable();
        boolean hasEnergyCapability(BlockEntity be, @Nullable Direction side);
        boolean hasEnergyCapability(ItemStack stack, @Nullable Direction side);
        boolean receiveEnergy(BlockEntity be, long amount, boolean simulate, @Nullable Direction side);
        boolean receiveEnergy(ItemStack stack, long amount, boolean simulate, @Nullable Direction side);
        long getMaxEnergyStored(BlockEntity be, @Nullable Direction side);
    }


    public static class MekanismCompat implements IEnergyCompat {
        @Override public boolean isUsable() { return true; }
        @Override public boolean hasEnergyCapability(BlockEntity be, @Nullable Direction side) {
            if (be == null) return false;
            String name = be.getClass().getName();
            return name.contains("Mekanism") || name.contains("Induction") || name.contains("EnergyCube");
        }
        @Override public boolean hasEnergyCapability(ItemStack stack, @Nullable Direction side) {
            return !stack.isEmpty() && stack.getItem().getClass().getName().contains("mekanism");
        }

        @Override public boolean receiveEnergy(BlockEntity be, long amount, boolean simulate, @Nullable Direction side) {
            if (simulate) return true;
            try {
                if (be.getClass().getName().contains("InductionCasing")) {
                    Object data = be.getClass().getMethod("getMultiblock").invoke(be);
                    if (data != null) {
                        Object container = data.getClass().getMethod("getEnergyContainer").invoke(data);
                        return fillMekContainer(container);
                    }
                }
                Method getContainers = be.getClass().getMethod("getEnergyContainers", Direction.class);
                List<?> containers = (List<?>) getContainers.invoke(be, side);
                if (containers != null) {
                    for (Object c : containers) fillMekContainer(c);
                    return true;
                }
            } catch (Exception ignored) {}
            return false;
        }

        @Override public boolean receiveEnergy(ItemStack stack, long amount, boolean simulate, @Nullable Direction side) {
            if (simulate) return true;
            try {
                Method getContainers = stack.getItem().getClass().getMethod("getEnergyContainers", ItemStack.class);
                List<?> containers = (List<?>) getContainers.invoke(stack.getItem(), stack);
                if (containers != null) {
                    for (Object c : containers) fillMekContainer(c);
                    return true;
                }
            } catch (Exception ignored) {}
            return false;
        }

        private boolean fillMekContainer(Object container) throws Exception {
            if (container == null) return false;
            Object max = container.getClass().getMethod("getMaxEnergy").invoke(container);
            Method setEnergy = container.getClass().getMethod("setEnergy", max.getClass());
            setEnergy.invoke(container, max);
            return true;
        }

        @Override public long getMaxEnergyStored(BlockEntity be, @Nullable Direction s) { return Long.MAX_VALUE; }
    }

    public static class IndustrialForegoingCompat implements IEnergyCompat {
        @Override public boolean isUsable() { return true; }

        @Override public boolean hasEnergyCapability(BlockEntity be, @Nullable Direction side) {
            return be != null && be.getClass().getName().contains("industrialforegoing");
        }

        @Override public boolean hasEnergyCapability(ItemStack stack, @Nullable Direction side) {
            if (stack.isEmpty()) return false;
            String name = stack.getItem().getClass().getName();
            return name.contains("ItemInfinity") || name.contains("Infinity");
        }

        @Override
        public boolean receiveEnergy(BlockEntity be, long amount, boolean simulate, @Nullable Direction side) {
            if (simulate) return true;
            try {
                Method getStorage = be.getClass().getMethod("getEnergyStorage");
                Object storage = getStorage.invoke(be);
                if (storage != null) {
                    long max = ((Number) storage.getClass().getMethod("getLongCapacity").invoke(storage)).longValue();
                    Method setEnergy = storage.getClass().getMethod("setEnergyStored", long.class);
                    setEnergy.setAccessible(true);
                    setEnergy.invoke(storage, max);
                    be.setChanged();
                    if (be.getLevel() != null) {
                        be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
                    }
                    return true;
                }
            } catch (Exception ignored) {}
            return false;
        }

        @Override
        public boolean receiveEnergy(ItemStack stack, long amount, boolean simulate, @Nullable Direction side) {
            if (simulate) return true;
            try {
                Class<?> tierEnum = Class.forName("com.buuz135.industrial.item.infinity.InfinityTier");
                Object artifact = null;
                for (Object obj : tierEnum.getEnumConstants()) {
                    if (obj.toString().equals("ARTIFACT")) {
                        artifact = obj;
                        break;
                    }
                }

                if (artifact != null) {
                    long powerNeeded = (long) artifact.getClass().getMethod("getPowerNeeded").invoke(artifact);

                    Class<?> attachmentsClass = Class.forName("com.buuz135.industrial.utils.IFAttachments");
                    Object powerKey = attachmentsClass.getField("INFINITY_ITEM_POWER").get(null);
                    Object tierKey = attachmentsClass.getField("INFINITY_ITEM_SELECTED_TIER").get(null);
                    Object canChargeKey = attachmentsClass.getField("INFINITY_ITEM_CAN_CHARGE").get(null);
                    Object specialKey = attachmentsClass.getField("INFINITY_ITEM_SPECIAL").get(null);
                    Object fuelKey = attachmentsClass.getField("INFINITY_ITEM_FUEL").get(null);

                    Method setMethod = stack.getClass().getMethod("set", powerKey.getClass().getInterfaces()[0], Object.class);

                    setMethod.invoke(stack, powerKey, powerNeeded);
                    setMethod.invoke(stack, tierKey, artifact);
                    setMethod.invoke(stack, canChargeKey, true);
                    setMethod.invoke(stack, specialKey, true);
                    setMethod.invoke(stack, fuelKey, 0);

                    return true;
                }
            } catch (Exception e) {
                return forceNbtCleanArtifact(stack);
            }
            return false;
        }

        private boolean forceNbtCleanArtifact(ItemStack stack) {
            try {
                Class<?> tierEnum = Class.forName("com.buuz135.industrial.item.infinity.InfinityTier");
                long artifactPower = 1_000_000_000_000L;
                for (Object obj : tierEnum.getEnumConstants()) {
                    if (obj.toString().equals("ARTIFACT")) {
                        artifactPower = (long) obj.getClass().getMethod("getPowerNeeded").invoke(obj);
                        break;
                    }
                }

                CompoundTag nbt = stack.getOrCreateTag();
                nbt.putLong("Energy", artifactPower);
                nbt.putInt("Fuel", 0); // 显式设为 0
                nbt.remove("Fluid");
                nbt.putBoolean("Special", true);
                nbt.putBoolean("CanCharge", true);
                nbt.putString("Selected", "ARTIFACT");
                return true;
            } catch (Exception ignored) { return false; }
        }

        @Override public long getMaxEnergyStored(BlockEntity be, @Nullable Direction s) {
            return 1L;
        }
    }


    public static class DraconicNetworkCompat implements IEnergyCompat {
        @Override public boolean isUsable() { return true; }
        @Override public boolean hasEnergyCapability(BlockEntity be, @Nullable Direction side) {
            if (be == null) return false;
            String name = be.getClass().getName().toLowerCase();
            return name.contains("draconic") || name.contains("tileenergycore") || name.contains("stabilizer");
        }
        @Override @SuppressWarnings("unchecked")
        public boolean receiveEnergy(BlockEntity be, long amount, boolean simulate, @Nullable Direction side) {
            if (simulate) return true;
            BlockEntity core = be;
            try {
                try {
                    Method getCore = be.getClass().getMethod("getCore");
                    Object coreObj = getCore.invoke(be);
                    if (coreObj instanceof BlockEntity beCore) core = beCore;
                } catch (Exception ignored) {}
                if (!core.getClass().getName().contains("TileEnergyCore")) return false;
                Field energyField = findField(core.getClass(), "energy");
                if (energyField != null) {
                    energyField.setAccessible(true);
                    Object opStorage = energyField.get(core);
                    if (opStorage != null) {
                        Field capField = findField(opStorage.getClass(), "capacity");
                        if (capField != null) {
                            capField.setAccessible(true);
                            BigInteger currentMax = BigInteger.ZERO;
                            Object rawSupplier = capField.get(opStorage);
                            if (rawSupplier instanceof Supplier<?> supplier) {
                                Object result = supplier.get();
                                if (result instanceof BigInteger bi) currentMax = bi;
                            }
                            if (currentMax.compareTo(BigInteger.valueOf(1000)) < 0) {
                                currentMax = BigInteger.valueOf(223).multiply(BigInteger.TEN.pow(55));
                            }
                            BigInteger newMax = currentMax.multiply(BigInteger.TEN);
                            capField.set(opStorage, (Supplier<BigInteger>) () -> newMax);
                            Field overflowField = findField(opStorage.getClass(), "overflowCount");
                            Field valueField = findField(opStorage.getClass(), "valueStorage");
                            if (overflowField != null && valueField != null) {
                                overflowField.setAccessible(true);
                                valueField.setAccessible(true);
                                BigInteger longMaxPlusOne = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
                                BigInteger[] parts = newMax.divideAndRemainder(longMaxPlusOne);
                                overflowField.set(opStorage, parts[0]);
                                valueField.setLong(opStorage, parts[1].longValue());
                            }
                            syncManagedData(core, newMax);
                        }
                    }
                }
                core.setChanged();
                if (core.getLevel() != null) {
                    core.getLevel().sendBlockUpdated(core.getBlockPos(), core.getBlockState(), core.getBlockState(), 3);
                }
                return true;
            } catch (Exception e) { return false; }
        }
        private void syncManagedData(BlockEntity core, BigInteger targetValue) {
            try {
                for (Field f : core.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    Object managed = f.get(core);
                    if (managed == null || !managed.getClass().getSimpleName().contains("Managed")) continue;
                    String fieldName = f.getName().toLowerCase();
                    if (fieldName.contains("percent")) {
                        Method setMethod = managed.getClass().getMethod("set", float.class);
                        setMethod.invoke(managed, 1.0f);
                    }
                    if (fieldName.contains("target")) {
                        try { managed.getClass().getMethod("set", BigInteger.class).invoke(managed, targetValue); }
                        catch (Exception e) { managed.getClass().getMethod("set", String.class).invoke(managed, targetValue.toString()); }
                    }
                }
            } catch (Exception ignored) {}
        }
        private Field findField(Class<?> clazz, String name) {
            while (clazz != null) {
                try { return clazz.getDeclaredField(name); }
                catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }
            }
            return null;
        }
        @Override public boolean hasEnergyCapability(ItemStack s, @Nullable Direction d) { return false; }
        @Override public boolean receiveEnergy(ItemStack s, long a, boolean sim, @Nullable Direction d) { return false; }
        @Override public long getMaxEnergyStored(BlockEntity be, @Nullable Direction s) { return Long.MAX_VALUE; }
    }

    public static class UniversalReflectionCompat implements IEnergyCompat {
        @Override public boolean isUsable() { return true; }
        @Override public boolean hasEnergyCapability(BlockEntity be, @Nullable Direction side) {
            if (be == null) return false;
            String name = be.getClass().getName();
            if (name.contains("TileEnergyCore")) return false;
            return name.contains("Duplicator") || name.contains("Charger");
        }
        @Override public boolean receiveEnergy(BlockEntity be, long amount, boolean simulate, @Nullable Direction side) {
            if (simulate) return true;
            try {
                for (Field field : be.getClass().getDeclaredFields()) {
                    String fName = field.getName().toLowerCase();
                    if (fName.contains("energy") || fName.contains("storage") || fName.contains("container")) {
                        field.setAccessible(true);
                        Object storageObj = field.get(be);
                        if (storageObj != null) {
                            forceFill(storageObj);
                            return true;
                        }
                    }
                }
            } catch (Exception ignored) {}
            return false;
        }
        private void forceFill(Object obj) {
            try {
                Method getMax = null;
                for (Method m : obj.getClass().getMethods()) {
                    if (m.getName().startsWith("getMaxEnergy")) { getMax = m; break; }
                }
                for (Method m : obj.getClass().getMethods()) {
                    if (m.getName().startsWith("setEnergy") && m.getParameterCount() == 1) {
                        m.setAccessible(true);
                        if (getMax != null) m.invoke(obj, getMax.invoke(obj));
                        else {
                            Class<?> p = m.getParameterTypes()[0];
                            if (p == long.class) m.invoke(obj, Long.MAX_VALUE);
                            else if (p == int.class) m.invoke(obj, Integer.MAX_VALUE);
                        }
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }
        @Override public boolean hasEnergyCapability(ItemStack s, @Nullable Direction d) { return false; }
        @Override public boolean receiveEnergy(ItemStack s, long a, boolean sim, @Nullable Direction d) { return false; }
        @Override public long getMaxEnergyStored(BlockEntity be, @Nullable Direction s) { return Long.MAX_VALUE; }
    }

    public static class ThermalUnlimitedCompat implements IEnergyCompat {
        @Override public boolean isUsable() { return true; }
        @Override public boolean hasEnergyCapability(BlockEntity be, Direction s) {
            return be != null && be.getCapability(ForgeCapabilities.ENERGY, s).isPresent();
        }
        @Override public boolean receiveEnergy(BlockEntity be, long amt, boolean sim, Direction s) {
            return be.getCapability(ForgeCapabilities.ENERGY, s).map(storage -> {
                if (sim) return true;
                try {
                    Method setMethod = findMethod(storage.getClass(), "setEnergyStored", int.class);
                    if (setMethod != null) {
                        setMethod.setAccessible(true);
                        setMethod.invoke(storage, storage.getMaxEnergyStored());
                        return true;
                    }
                } catch (Exception ignored) {}
                return storage.receiveEnergy((int) Math.min(amt, Integer.MAX_VALUE), sim) > 0;
            }).orElse(false);
        }
        private Method findMethod(Class<?> clazz, String name, Class<?>... params) {
            while (clazz != null) {
                try { return clazz.getDeclaredMethod(name, params); } catch (NoSuchMethodException e) { clazz = clazz.getSuperclass(); }
            }
            return null;
        }
        @Override public boolean hasEnergyCapability(ItemStack st, Direction s) { return false; }
        @Override public boolean receiveEnergy(ItemStack st, long a, boolean s, Direction d) { return false; }
        @Override public long getMaxEnergyStored(BlockEntity be, Direction s) {
            return be.getCapability(ForgeCapabilities.ENERGY, s).map(cap -> (long)cap.getMaxEnergyStored()).orElse(0L);
        }
    }

    public static class ForgeEnergyCompat implements IEnergyCompat {
        public boolean isUsable() { return true; }
        public boolean hasEnergyCapability(BlockEntity be, Direction s) {
            return be != null && !be.isRemoved() && be.getCapability(ForgeCapabilities.ENERGY, s).isPresent();
        }
        public boolean hasEnergyCapability(ItemStack st, Direction s) {
            return !st.isEmpty() && st.getCapability(ForgeCapabilities.ENERGY, s).isPresent();
        }
        public boolean receiveEnergy(BlockEntity be, long amt, boolean sim, Direction s) {
            return be.getCapability(ForgeCapabilities.ENERGY, s).map(cap -> cap.receiveEnergy((int)Math.min(amt, Integer.MAX_VALUE), sim) > 0).orElse(false);
        }
        public boolean receiveEnergy(ItemStack st, long amt, boolean sim, Direction s) {
            return st.getCapability(ForgeCapabilities.ENERGY, s).map(cap -> cap.receiveEnergy((int)Math.min(amt, Integer.MAX_VALUE), sim) > 0).orElse(false);
        }
        public long getMaxEnergyStored(BlockEntity be, Direction s) {
            return be.getCapability(ForgeCapabilities.ENERGY, s).map(cap -> (long)cap.getMaxEnergyStored()).orElse(0L);
        }
    }
}
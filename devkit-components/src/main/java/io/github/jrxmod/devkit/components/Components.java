package io.github.jrxmod.devkit.components;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Fluent factory for Minecraft 1.21+ {@link ComponentType} instances.
 * <p>
 * Reduces Data Component registration boilerplate and ensures
 * consistent codec / packetCodec pairing for server-client synchronization.
 *
 * @author jrxmod
 * @since 0.1.0
 */
public final class Components {
    private Components() {}

    /**
     * Creates a new component builder with explicit namespace.
     *
     * @param modId owning mod identifier
     * @param path  component path
     * @param codec persistent NBT codec
     * @param <T>   component value type
     * @return configurable builder
     */
    public static <T> ComponentBuilder<T> builder(String modId, String path, Codec<T> codec) {
        return new ComponentBuilder<>(Identifier.of(modId, path), codec);
    }

    /**
     * Convenience builder for integer components.
     * Network codec defaults to {@link PacketCodecs#VAR_INT}.
     *
     * @param modId mod namespace
     * @param path  component path
     * @return integer component builder
     */
    public static ComponentBuilder<Integer> intComponent(String modId, String path) {
        return new ComponentBuilder<>(Identifier.of(modId, path), Codec.INT)
                .networked(PacketCodecs.VAR_INT);
    }

    /**
     * @deprecated Use {@link #intComponent(String, String)} to avoid
     *             unregistered identifiers in production code.
     */
    @Deprecated
    public static ComponentBuilder<Integer> intComponent(String path) {
        return new ComponentBuilder<>(null, Codec.INT)
                .networked(PacketCodecs.VAR_INT);
    }

    /**
     * Fluent builder for {@link ComponentType}.
     *
     * @param <T> value type
     */
    public static final class ComponentBuilder<T> {
        private final Identifier id;
        private final Codec<T> codec;
        private PacketCodec<? super RegistryByteBuf, T> packetCodec;
        private boolean cache;

        ComponentBuilder(Identifier id, Codec<T> codec) {
            this.id = id;
            this.codec = codec;
        }

        /**
         * Attaches a network synchronization codec.
         *
         * @param packetCodec S2C packet codec
         * @return this builder
         */
        public ComponentBuilder<T> networked(PacketCodec<? super RegistryByteBuf, T> packetCodec) {
            this.packetCodec = packetCodec;
            return this;
        }

        /**
         * Enables value caching inside the component system.
         * Recommended for immutable value types.
         *
         * @return this builder
         */
        public ComponentBuilder<T> cache() {
            this.cache = true;
            return this;
        }

        /**
         * Marks the component as persistent (default).
         * Retained for fluent API symmetry.
         *
         * @return this builder
         */
        public ComponentBuilder<T> persistent() {
            return this;
        }

        /**
         * Builds an unregistered {@link ComponentType} instance.
         *
         * @return built component type
         */
        public ComponentType<T> build() {
            ComponentType.Builder<T> builder = ComponentType.<T>builder().codec(codec);
            if (packetCodec != null) {
                builder.packetCodec(packetCodec);
            }
            if (cache) {
                builder.cache();
            }
            return builder.build();
        }

        /**
         * Builds and immediately registers the component type
         * into {@link Registries#DATA_COMPONENT_TYPE}.
         *
         * @return registered component type
         * @throws IllegalStateException if {@code id} is null
         */
        public ComponentType<T> buildAndRegister() {
            if (id == null) {
                throw new IllegalStateException("Component identifier must not be null for direct registration – use builder(modId, path, codec)");
            }
            return Registry.register(Registries.DATA_COMPONENT_TYPE, id, build());
        }

        /**
         * Builds and registers into a supplied registry instance.
         * Useful for test harnesses and datagen environments.
         *
         * @param registry target registry
         * @return registered component type
         */
        public ComponentType<T> buildAndRegister(Registry<ComponentType<?>> registry) {
            if (id == null) {
                throw new IllegalStateException("Component identifier is required for registry insertion");
            }
            ComponentType<T> type = build();
            return Registry.register(registry, id, type);
        }
    }
}

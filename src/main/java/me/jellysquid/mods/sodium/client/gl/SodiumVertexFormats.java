package me.jellysquid.mods.sodium.client.gl;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttribute;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeFormat;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadEncoder;

public class SodiumVertexFormats {
    /**
     * Simple vertex format which uses single-precision floating point numbers to represent position and texture
     * coordinates.
     */
    public static final GlVertexFormat<ChunkMeshAttribute> CHUNK_MESH_FULL = GlVertexAttribute.builder(ChunkMeshAttribute.class, 32)
            .addElement(ChunkMeshAttribute.POSITION, 0, GlVertexAttributeFormat.FLOAT, 3, false)
            .addElement(ChunkMeshAttribute.COLOR, 12, GlVertexAttributeFormat.UNSIGNED_BYTE, 4, true)
            .addElement(ChunkMeshAttribute.TEXTURE, 16, GlVertexAttributeFormat.FLOAT, 2, false)
            .addElement(ChunkMeshAttribute.LIGHT, 24, GlVertexAttributeFormat.UNSIGNED_SHORT, 2, false)
            .build();

    /**
     * Uses half-precision floating point numbers to represent position coordinates and normalized unsigned shorts for
     * texture coordinates. All texel positions in the block diffuse texture atlas can be exactly mapped (including
     * their centering offset), as the
     */
    public static final GlVertexFormat<ChunkMeshAttribute> CHUNK_MESH_COMPACT = GlVertexAttribute.builder(ChunkMeshAttribute.class, 20)
            .addElement(ChunkMeshAttribute.POSITION, 0, GlVertexAttributeFormat.UNSIGNED_SHORT, 3, true)
            .addElement(ChunkMeshAttribute.COLOR, 8, GlVertexAttributeFormat.UNSIGNED_BYTE, 4, true)
            .addElement(ChunkMeshAttribute.TEXTURE, 12, GlVertexAttributeFormat.UNSIGNED_SHORT, 2, true)
            .addElement(ChunkMeshAttribute.LIGHT, 16, GlVertexAttributeFormat.UNSIGNED_SHORT, 2, false)
            .build();

    private static final Reference2ObjectMap<GlVertexFormat<?>, ModelQuadEncoder> encoders = new Reference2ObjectOpenHashMap<>();

    static {
        registerEncoder(CHUNK_MESH_FULL, (quad, buffer, position) -> {
            for (int i = 0; i < 4; i++) {
                buffer.putFloat(position, quad.getX(i));
                buffer.putFloat(position + 4, quad.getY(i));
                buffer.putFloat(position + 8, quad.getZ(i));
                buffer.putInt(position + 12, quad.getColor(i));
                buffer.putFloat(position + 16, quad.getTexU(i));
                buffer.putFloat(position + 20, quad.getTexV(i));
                buffer.putInt(position + 24, quad.getLight(i));

                position += 32;
            }
        });

        registerEncoder(CHUNK_MESH_COMPACT, (quad, buffer, position) -> {
            for (int i = 0; i < 4; i++) {
                buffer.putShort(position, denormalizeFloatAsShort(quad.getX(i)));
                buffer.putShort(position + 2, denormalizeFloatAsShort(quad.getY(i)));
                buffer.putShort(position + 4, denormalizeFloatAsShort(quad.getZ(i)));
                buffer.putInt(position + 8, quad.getColor(i));
                buffer.putShort(position + 12, denormalizeFloatAsShort(quad.getTexU(i)));
                buffer.putShort(position + 14, denormalizeFloatAsShort(quad.getTexV(i)));
                buffer.putInt(position + 16, quad.getLight(i));

                position += 20;
            }
        });
    }

    public static void registerEncoder(GlVertexFormat<?> format, ModelQuadEncoder encoder) {
        if (encoders.containsKey(format)) {
            throw new IllegalStateException("Encoder already registered for format: " + format);
        }

        encoders.put(format, encoder);
    }

    public static ModelQuadEncoder getEncoder(GlVertexFormat<?> format) {
        ModelQuadEncoder encoder = encoders.get(format);

        if (encoder == null) {
            throw new NullPointerException("No encoder exists for format: " + format);
        }

        return encoder;
    }

    public enum ChunkMeshAttribute {
        POSITION,
        COLOR,
        TEXTURE,
        LIGHT
    }

    /**
     * Converts a floating point in normalized range to a de-normalized unsigned short.
     * @param value The normalized float
     * @return The resulting de-normalized unsigned short
     */
    private static short denormalizeFloatAsShort(float value) {
        return (short) (value * 65536.0f);
    }
}

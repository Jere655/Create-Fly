package com.zurrtum.create.client.flywheel.backend.gl;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlSampler;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;

public final class GlGpuResources {
    private GlGpuResources() {
    }

    public static GlTexture requireGlTexture(GpuTexture texture) {
        if (texture instanceof ValidationGpuTexture validationTexture) {
            texture = validationTexture.getRealTexture();
        }

        if (texture instanceof GlTexture glTexture) {
            return glTexture;
        }

        throw new IllegalStateException("Expected an OpenGL-backed GPU texture, got "
            + (texture == null ? "null" : texture.getClass().getName()));
    }

    public static GlSampler requireGlSampler(GpuSampler sampler) {
        if (sampler instanceof GlSampler glSampler) {
            return glSampler;
        }

        throw new IllegalStateException("Expected an OpenGL-backed GPU sampler, got "
            + (sampler == null ? "null" : sampler.getClass().getName()));
    }

    public static GlDevice requireGlDevice(GpuDevice device) {
        if (device instanceof ValidationGpuDevice validationDevice) {
            device = validationDevice.getRealDevice();
        }

        if (device instanceof GlDevice glDevice) {
            return glDevice;
        }

        throw new IllegalStateException("Expected an OpenGL-backed GPU device, got "
            + (device == null ? "null" : device.getClass().getName()));
    }
}

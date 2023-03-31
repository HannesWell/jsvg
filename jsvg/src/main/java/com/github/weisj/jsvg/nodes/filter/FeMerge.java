/*
 * MIT License
 *
 * Copyright (c) 2023 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.github.weisj.jsvg.nodes.filter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.nodes.container.ContainerNode;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.renderer.RenderContext;

@ElementCategories(Category.FilterPrimitive)
@PermittedContent(
    anyOf = {FeMergeNode.class}
)
public class FeMerge extends ContainerNode implements FilterPrimitive {
    public static final String TAG = "feMerge";

    private FilterPrimitiveBase filterPrimitiveBase;
    private Object[] inputChannels;

    @Override
    public @NotNull String tagName() {
        return TAG;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);

        filterPrimitiveBase = new FilterPrimitiveBase(attributeNode);

        List<FeMergeNode> nodes = childrenOfType(FeMergeNode.class);
        inputChannels = new Object[nodes.size()];
        for (int i = 0; i < inputChannels.length; i++) {
            inputChannels[i] = nodes.get(i).inputChannel();
        }
    }

    @Override
    public boolean isValid() {
        return inputChannels.length > 0;
    }

    public @NotNull Length x() {
        return filterPrimitiveBase.x;
    }

    public @NotNull Length y() {
        return filterPrimitiveBase.y;
    }

    public @NotNull Length width() {
        return filterPrimitiveBase.width;
    }

    public @NotNull Length height() {
        return filterPrimitiveBase.height;
    }

    @Override
    public void applyFilter(@NotNull RenderContext context, @NotNull FilterContext filterContext) {
        if (inputChannels.length == 0) return;
        Channel in = filterPrimitiveBase.channel(inputChannels[0], filterContext);
        Channel result = in;
        if (inputChannels.length > 1) {
            BufferedImage dst = in.toBufferedImageNonAliased(context);
            Graphics2D imgGraphics = (Graphics2D) dst.getGraphics();
            for (int i = 1; i < inputChannels.length; i++) {
                Channel channel = filterPrimitiveBase.channel(inputChannels[i], filterContext);
                imgGraphics.drawImage(context.createImage(channel.producer()), null, context.targetComponent());
            }
            result = new ImageProducerChannel(dst.getSource());
        }
        filterPrimitiveBase.saveResult(result, filterContext);
    }
}
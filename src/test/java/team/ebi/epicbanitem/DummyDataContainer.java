/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package team.ebi.epicbanitem;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

/**
 * The default implementation of {@link DataContainer} that can be instantiated
 * for any use. This is the primary implementation of any {@link DataView} that
 * is used throughout both SpongeAPI and Sponge implementation.
 */
public final class DummyDataContainer extends DummyDataView implements DataContainer {

  /**
   * Creates a new {@link DummyDataContainer} with a default
   * {@link org.spongepowered.api.data.persistence.DataView.SafetyMode} of
   * {@link org.spongepowered.api.data.persistence.DataView.SafetyMode#ALL_DATA_CLONED}.
   *
   */
  public DummyDataContainer() {
    this(DataView.SafetyMode.ALL_DATA_CLONED);
  }

  /**
   * Creates a new {@link DummyDataContainer} with the provided
   * {@link org.spongepowered.api.data.persistence.DataView.SafetyMode}.
   *
   * @param safety The safety mode to use
   * @see org.spongepowered.api.data.persistence.DataView.SafetyMode
   */
  public DummyDataContainer(final DataView.SafetyMode safety) {
    super(safety);
  }

  @Override
  public Optional<DataView> parent() {
    return Optional.empty();
  }

  @Override
  public DataContainer container() {
    return this;
  }

  @Override
  public DataContainer set(final DataQuery path, final Object value) {
    return (DataContainer) super.set(path, value);
  }

  @Override
  public DataContainer remove(final DataQuery path) {
    return (DataContainer) super.remove(path);
  }

}

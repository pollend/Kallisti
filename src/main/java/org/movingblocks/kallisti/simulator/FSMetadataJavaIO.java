/*
 * Copyright 2018 Adrian Siekierka, MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.movingblocks.kallisti.simulator;

import org.movingblocks.kallisti.base.interfaces.FileSystem;

import java.util.Date;

public class FSMetadataJavaIO implements FileSystem.Metadata {
    private final java.io.File file;

    public FSMetadataJavaIO(java.io.File file) {
        this.file = file;
    }

    @Override
    public String name() {
        return file.getName();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public boolean canRead() {
        return file.canRead();
    }

    @Override
    public boolean canWrite() {
        return file.canWrite();
    }

    @Override
    public Date creationTime() {
        return modificationTime(); // TODO
    }

    @Override
    public Date modificationTime() {
        return new Date(file.lastModified());
    }

    @Override
    public long size() {
        return file.length();
    }
}

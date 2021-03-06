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

import org.movingblocks.kallisti.oc.*;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        SimulatorInstantiationManager manager = new SimulatorInstantiationManager();
        manager.register("MachineOpenComputers", (owner, context, json) -> new MachineOpenComputers(
                new File(json.get("machineFile").getAsString()),
                context,
                new OCFont(
                        new File(json.get("font").getAsString()),
                        json.get("fontHeight").getAsInt()
                )
        ));

        manager.register("InMemoryStaticByteStorage", (owner, context, json) -> new InMemoryStaticByteStorage(
                json.get("file").getAsString(),
                json.get("size").getAsInt()
        ));

        manager.register("SimulatorFileSystem", (owner, context, json) -> new SimulatorFileSystem(
                json.get("base").getAsString()
        ));

        manager.register("SimulatorFrameBufferWindow", (owner, context, json) -> new SimulatorFrameBufferWindow(
                json.get("windowName").getAsString()
        ));

        manager.register("SimulatorKeyboardInputWindow", (owner, context, json) -> new SimulatorKeyboardInputWindow(
                json.get("windowName").getAsString()
        ));

        manager.register("PeripheralOCGPU", (owner, context, json) -> new PeripheralOCGPU(
                (MachineOpenComputers) owner,
                json.get("maxWidth").getAsInt(),
                json.get("maxHeight").getAsInt(),
                PeripheralOCGPU.genThirdTierPalette()
        ));

        Simulator simulator = new Simulator(manager, new File(args[0]));
        simulator.start();

        boolean tick = true;

        while (tick) {
            long t = System.currentTimeMillis();
            tick = simulator.tick();
            int l = (int) (simulator.getTickDuration() * 1000) - ((int) (System.currentTimeMillis() - t));
            if (l > 0) {
                Thread.sleep(l);
            }
        }
    }
}

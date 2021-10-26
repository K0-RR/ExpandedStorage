/**
 * Copyright 2021 NinjaPhenix
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
package ninjaphenix.expandedstorage.block.misc.strategies;

import net.minecraft.entity.player.PlayerEntity;

public interface Observable {
    Observable NOT = new Observable() {
        @Override
        public void playerStartViewing(PlayerEntity player) {

        }

        @Override
        public void playerStopViewing(PlayerEntity player) {

        }

        @Override
        public boolean isViewedBy(PlayerEntity player) {
            return false;
        }
    };

    void playerStartViewing(PlayerEntity player);

    void playerStopViewing(PlayerEntity player);

    boolean isViewedBy(PlayerEntity player);
}

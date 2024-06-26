/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.util;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface Task extends Runnable {

    abstract class Base implements Task {

        protected final HuskTowns plugin;
        protected final Runnable runnable;
        protected boolean cancelled = false;

        protected Base(@NotNull HuskTowns plugin, @NotNull Runnable runnable) {
            this.plugin = plugin;
            this.runnable = runnable;
        }

        public void cancel() {
            cancelled = true;
        }

        @NotNull
        @Override
        public HuskTowns getPlugin() {
            return plugin;
        }

    }

    abstract class Async extends Base {

        protected long delayTicks;

        protected Async(@NotNull HuskTowns plugin, @NotNull Runnable runnable, long delayTicks) {
            super(plugin, runnable);
            this.delayTicks = delayTicks;
        }

    }

    abstract class Sync extends Base {

        protected long delayTicks;

        protected Sync(@NotNull HuskTowns plugin, @NotNull Runnable runnable, long delayTicks) {
            super(plugin, runnable);
            this.delayTicks = delayTicks;
        }

    }

    abstract class Repeating extends Base {

        protected final long repeatingTicks;

        protected Repeating(@NotNull HuskTowns plugin, @NotNull Runnable runnable, long repeatingTicks) {
            super(plugin, runnable);
            this.repeatingTicks = repeatingTicks;
        }

    }

    @SuppressWarnings("UnusedReturnValue")
    interface Supplier {

        @NotNull
        Task.Sync getSyncTask(@NotNull Runnable runnable, @Nullable OnlineUser user, long delayTicks);

        @NotNull
        Task.Async getAsyncTask(@NotNull Runnable runnable, long delayTicks);

        @NotNull
        Task.Repeating getRepeatingTask(@NotNull Runnable runnable, long repeatingTicks);

        @NotNull
        default Task.Sync runSyncDelayed(@NotNull Runnable runnable, @Nullable OnlineUser user, long delayTicks) {
            final Task.Sync task = getSyncTask(runnable, user, delayTicks);
            task.run();
            return task;
        }

        @NotNull
        default Task.Async runAsyncDelayed(@NotNull Runnable runnable, long delayTicks) {
            final Task.Async task = getAsyncTask(runnable, delayTicks);
            task.run();
            return task;
        }

        @NotNull
        default Task.Sync runSync(@NotNull Runnable runnable) {
            return runSyncDelayed(runnable, null, 0);
        }

        @NotNull
        default Task.Sync runSync(@NotNull Runnable runnable, @NotNull OnlineUser user) {
            return runSyncDelayed(runnable, user, 0);
        }

        @NotNull
        default Task.Async runAsync(@NotNull Runnable runnable) {
            final Task.Async task = getAsyncTask(runnable, 0);
            task.run();
            return task;
        }

        default <T> CompletableFuture<T> supplyAsync(@NotNull java.util.function.Supplier<T> supplier) {
            final CompletableFuture<T> future = new CompletableFuture<>();
            runAsync(() -> {
                try {
                    future.complete(supplier.get());
                } catch (Throwable throwable) {
                    future.completeExceptionally(throwable);
                }
            });
            return future;
        }

        void cancelTasks();

        @NotNull
        HuskTowns getPlugin();

    }

    @NotNull
    HuskTowns getPlugin();

}
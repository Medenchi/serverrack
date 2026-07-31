# Server Rack Mod — Fabric 1.21.1

Реалистичные серверные стойки с анимацией и динамическим дымом по потолку.

## Что есть

**Три варианта стоек разного размера (мультиблок):**

- `server_rack_basic` — **Small 2 блока** в высоту (LOWER/UPPER). Маленькая стойка.
- `server_rack_advanced` — **Big 3 блока** в высоту (LOWER/MIDDLE/UPPER). Высокая стойка.
- `server_rack_mainframe` — **Mainframe 2x2** (4 блока: LOWER_LEFT, LOWER_RIGHT, UPPER_LEFT, UPPER_RIGHT) — 2 в высоту и 2 в ширину, толстый мейнфрейм.

Все стойки:
- Направленные (FACING), можно вращать.
- Имеют свойство ACTIVE (вкл/выкл).
- Светятся сильнее когда активны (luminance 10-13).
- Имеют BlockEntity только у нижней/главной части.
- Ломание одной части ломает весь мультиблок.

**Анимация через BlockEntityRenderer:**

- Вращение вентиляторов (фан-блейды) — реальное 3D вращение по времени, быстрее когда активно, два вентилятора для больших стоек.
- Мигание LED — синий быстро, зеленый медленно, красный постоянно горит когда активно. Эмиссивное свечение.
- Текстуры 64x64 детальные, реалистичный дата-центр: черный матовый металл, вентиляционные решетки, цветные LED.

**Дым:**

- Активируется клавишей **J (Ж)** — тогглит ближайшую стойку под прицелом (raycast) или в радиусе 5 блоков.
- Каждая активная стойка спавнит rising частицы + кормит `CeilingSmokeManager`.
- `CeilingSmokeManager` находит потолок над стойкой в пределах `maxRiseHeight`, затем накапливает плотность в точке потолка и распространяет BFS по потолку в радиусе `ceilingRadius`. Только если над воздухом есть твердый блок.
- Плотность ограничена `maxDensity`, спавнятся drifting частицы под потолком пропорционально плотности.
- Дым затухает (decay) — быстрее если потолок сломан.
- При удалении стойки дым перестает подпитываться и исчезает.
- Без урона, чисто визуальный.

**Меню настроек — клавиша U (Г):**

- Экран `SmokeConfigScreen` с полями:
  - Частота (тиков) 1-40
  - Кол-во частиц 1-32
  - Радиус по потолку 1-24
  - Высота подъема 2-64
  - Макс плотность 1-200
- Кнопка "Сохранить" — обновляет глобальный `SmokeConfig` и синкает на сервер через `SmokeConfigPayload` → `SmokeConfigSyncPayload` всем игрокам.
- Кнопка "Удалить весь дым" — отправляет `ClearSmokePayload` → сервер бродкастит `ClearSmokeSyncPayload`, клиент очищает `CeilingSmokeManager.clearAll()`.
- Кнопка сброса по умолчанию.

**Сеть:**

- Fabric payloads: `smoke_config`, `smoke_config_sync`, `toggle_rack`, `clear_smoke_c2s`, `clear_smoke_s2c`
- Сервер хранит актуальный конфиг, синкает при подключении.
- Tоггл стоек — серверный, звук BEACON_ACTIVATE/DEACTIVATE.

**Креативная вкладка:**

- `Server Racks` с тремя блоками, без крафта.

**Текстуры и модели:**

- 64x64 детальные текстуры для стоек (basic/advanced/mainframe) + fan_blade, led_overlay
- Частица `ceiling_smoke` 32x32 + JSON в `particles/`
- Модели `cube_all` + active variants + item models ссылаются на block models
- Blockstates с ротацией по facing и half/part/active
- Иконка мода `icon.png`

## Управление

- **J** — вкл/выкл ближайшую стойку
- **U** — меню дыма

## Сборка

Требуется Java 21, Gradle 8.14.2.

```bash
./gradlew build
```

Готовый jar в `build/libs/serverrack-1.0.0.jar`

Зависимости выкачиваются с `maven.fabricmc.net` — нужен интернет.

## Структура кода

- `block/` — 3 мультиблока + enums RackHalf, TripleBlockPart, MainframePart
- `blockentity/ServerRackBlockEntity` — serverTick/clientTick, спавн частиц, кормление CeilingSmokeManager
- `smoke/` — SmokeConfig (глобальные настройки), CeilingSmokeManager (накопление по потолку, BFS spread, decay)
- `particle/` — CeilingSmokeParticle (rise → hit ceiling → drift)
- `registry/` — ModBlocks, ModBlockEntities, ModParticles, ModItems, ModItemGroups, ModNetworking
- `network/payload/` — 5 payloads
- `client/` — ServerRackClient (keybinds, particle factory, BER, client networking, smoke tick), render/BER, screen/ConfigScreen

## TODO / идеи

- Добавить звуковой гул вентиляторов когда активно
- Добавить emissive текстурки с помощью Optifine/Continuity
- Добавить WAILA/JADE подсказки с состоянием ACTIVE и плотностью дыма

Сделано by Denchy + ehalo / Agent Mode.

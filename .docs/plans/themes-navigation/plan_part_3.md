# Темы 70-х и навигация. Часть 3: заставка и системные бары

Создан: 2026-11-15 21:01

Общие решения, модули и зависимости описаны в [части 1](plan_part_1.md). Это отдельный коммит.

### 12. Заставка
- Подключить `core-splashscreen`.
- Тема окна наследуется от `Theme.SplashScreen`: один нейтральный фон `#F3E6D0` и `postSplashScreenTheme`.
- `setKeepOnScreenCondition { selector.choice.value == null }`, чтобы заставка держалась, пока тема не загружена.

### 13. Системные бары
- В корне `setContent` вызывать `LaunchedEffect(palette) { enableEdgeToEdge(SystemBarStyle.light/dark(...)) }` по светлоте `background` активной палитры. Так бары обновляются при каждой смене темы.

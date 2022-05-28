# Редактор для работы с естественными языками

Редактор реализован с использованием Kotlin Multiplatform и фреймворков Jetpack Compose и Compose for Desktop.

## Запуск через Intellij IDEA

Для запуска десктопной версии исполните файл Main.kt в модуле desktop.

Для запуска Android версии исполните файл MainActivity.kt в модуле android.

## Руководство для встраивания собственного сервиса проверки орфографии и автодополнения

Для того, чтобы встроить собственный сервис, нужно реализовать интерфейс [TextAnalyzer](https://github.com/jeinygroove/highlight-editor-multiplatform/blob/master/common/src/commonMain/kotlin/com/highlightEditor/editor/diagnostics/TextAnalyzer.kt)

- метод analyze 

На вход приходит список предложений, на выход нужно вернуть список объектов [Diagnostic 
Element](https://github.com/jeinygroove/highlight-editor-multiplatform/blob/master/common/src/commonMain/kotlin/com/highlightEditor/editor/diagnostics/DiagnosticElement.kt)

- метод autocomplete

На вход приходит контекст (в текущей реализации редактора это весь текст) и префикс, который надо дополнить (это суффикс после последнего пробела в тексте). Метод должен вернуть список строк, дополняющих данный префикс. В редакторе будет отображен первый элемент списка.

В качестве реализации по умолчанию можно вернуть список, состоящий из одной пустой строки.

После реализации интерфейса его нужно передать:

- для desktop: в качестве аргумента в функцию EditorApplication, предварительно обернув в rememberApplicationState. Место в коде, куда нужно передать реализацию интерфейса, и пример представлены [здесь](https://github.com/jeinygroove/highlight-editor-multiplatform/blob/master/desktop/src/jvmMain/kotlin/Main.kt)

- для Android: аналогично передать в функцию App, представленную [здесь](https://github.com/jeinygroove/highlight-editor-multiplatform/blob/master/android/src/main/java/me/olgashimanskaia/android/MainActivity.kt)

Интерфейс нужно реализовать для каждой из платформ и поместить в модули [androidMain](https://github.com/jeinygroove/highlight-editor-multiplatform/tree/master/common%2Fsrc%2FandroidMain) и [desktopMain](https://github.com/jeinygroove/highlight-editor-multiplatform/tree/master/common%2Fsrc%2FdesktopMain) для Android и десктопа соответственно.

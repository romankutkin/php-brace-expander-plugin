# PHP Brace Expander

PHP Brace Expander is a PhpStorm plugin for writing class-like declarations in
the [PER Coding Style 3.1](https://www.php-fig.org/per/coding-style/) layout.

When Enter is pressed between the braces of an empty named class, interface,
trait, or enum, the plugin changes this:

```php
interface Something {<caret>}
```

into this:

```php
interface Something
{
    <caret>
}
```

Control structures and anonymous classes retain PhpStorm's normal brace
placement. The plugin delegates indentation and caret placement to PhpStorm,
so the edit participates in the normal Enter action and Undo history.

## Development

Run the automated tests:

```shell
./gradlew test
```

Start a development instance of PhpStorm with the plugin installed:

```shell
./gradlew runIde
```

Build the distributable plugin archive:

```shell
./gradlew buildPlugin
```

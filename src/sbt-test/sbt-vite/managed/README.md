# Example: DependencyManagement.Managed

This project uses sbt-vite in "managed" mode to build a complex multi-language 
frontend project.

The project is a calculator SPA built using React. Most of it is written in Scala.js,
but the button and display components are JavaScript, as is the top-level script that
mounts the top-level react component. The display component also imports a utility
exported from Scala.js. It thus demonstrates bidirectional interoperability between
Scala.js and JavaScript.

## Project structure

```
├── README.md
├── build.sbt
├── project
├── test
└── src
     ├── main
     │     ├── entrypoint
     │     ├── javascript
     │     ├── scala
     │     └── styles
     └── test
           └── scala
```

Notice that we have our `entrypoint` artifacts in the source directory
(src/main/entrypoint). This includes the `index.html` that vite will use to 
to generate the bundle. In a typical project, these artifacts would be at the 
project root, but since we are in "managed" mode, we can add 
`src/main/entrypoint` as a managed source (see build settings, below), and 
vite will treat its contents as though they are at the project root.

Notice also that there is no `package.json` or `node_modules` here: npm 
dependencies are managed entirely behind the scenes.

## Build settings

The settings specific to sbt-vite are as follows:

```sbt
viteOtherSources += Location.FromProject(file("src/main/javascript"))
viteOtherSources += Location.FromProject(file("src/main/entrypoint"))
viteOtherSources += Location.FromProject(file("src/main/styles"))

npmDependencies ++= Seq(
	"react" -> "^18.2.0",
	"react-dom" -> "^18.2.0",
	"prop-types" -> "^15.8.1",
	"react-toastify" -> "^6.0.8",
)
```

In `viteOtherSources` we include a directory containing some javascript code,
a directory with our entry points (the html file and the top-level script), and a 
directory containing some styles (css).

In `npmDependencies`, we include the various javascript libraries that our Scala.js 
and javascript code depends on. Notice we don't specify any `npmDevDependencies`. 
The JS dependencies needed for development (e.g., vite, lodash), are included by default.

Note that we don't have to set `viteDependencyManagement` since it defaults to 
`DependencyManagement.Managed(NpmManager.Npm)`.

## Dependency resolution

### NPM dependencies

In both Scala.js and javascript files, npm dependencies are imported in the 
usual way, e.g.,

```javascript
// From src/main/javascript/component/Button.jsx
import React, { Component } from 'react';
import PropTypes from 'prop-types';
...
```

sbt-vite will handle the installation of these dependencies within the build context

### Local dependencies

Dependencies from any of the directories included in `viteOtherSources` can simply be 
imported relative to those directories:

```scala
// From src/main/scala/example/components/Display.scala
@js.native
@JSImport("/buttonPanel.css", JSImport.Namespace)
object ButtonPanelCss extends js.Object

@js.native
@JSImport("/component/Button.jsx", JSImport.Default)
object ButtonRaw extends js.Object
```

Since `Button.jsx` is in `src/main/javascript/component`, which is included in
`viteOtherSources`, we can import it without referring to `src/main/javascript`.
(NB: the leading slash is required to work properly.) Similarly, `/buttonPanel.css` 
is resolvable because it is in `src/main/styles`, which is included in 
`viteOtherSources`.

### Scala.js dependencies

The above example shows how a Scala.js file can import a non-Scala.js source 
declared in `viteOtherSources`, but what about importing in the other direction?
That is, how do import from Scala.js into a JavaScript or TypeScript file? 

This can be accomplished by prefixing imports with `scalajs:`. Vite will resolve this 
as the output directory of `fullLinkJS` or `fastLinkJS` (depending on whether you are 
using `viteBuildProd` or `viteBuildDev`). When there is nothing after the colon, it 
is equivalent to `scalajs:main.js` (`main.js` is the main output of the Scala.js linker).

For instance, the entrypoint script in this project pulls the top level react app from 
our Scala.js source as follows:

```javascript
// From src/main/entrypoint/main.jsx
import React from 'react';
import { createRoot } from 'react-dom/client';

import App from 'scalajs:';

const root = createRoot(document.getElementById('app'))
root.render(<App />, );
```

The above import will work because our Scala.js code includes a top-level default 
export:

```scala
// From src/main/scala/example/App.scala

	@JSExportTopLevel("default")
	val rawApp =
		component
		  .cmapCtorProps[Unit](identity) // Change props from JS to Scala
		  .toJsComponent // Create a new, real JS component
		  .raw // Leave the nice Scala wrappers behind and obtain the underlying JS value
```

It is also possible to separate Scala.js exports into separate modules by specifying a
`moduleId`. For instance, the following exports a utility function to a separate module:

```scala
// From src/main/scala/example/util/FormatNumber.scala
	@JSExportTopLevel("formatNumberString", "utils")
	def formatNumberString(numberString: String): String = numberString match {
		case Value.NonDecimal() => addCommasNonDecimal(numberString)
		case Value.Decimal(nonDecimal, decimal) =>
			val nonDecimalWithCommas = addCommasNonDecimal(nonDecimal)
			s"$nonDecimalWithCommas.$decimal"
		case "" => "0"
		case _ => throw new RuntimeException(s"Invalid number string: $numberString")
	}
```

Since we specify a `moduleId` of "utils" in the second parameter of
`JSExportTopLevel`, the Scala.js linker will create a second file called `utils.js` 
that exports the annotated function. We can then import it as follows:

```javascript
// From src/main/javascript/component/Display.jsx
import { formatNumberString } from 'scalajs:utils.js'
```

## Building and testing

To build the project, run

```shell
sbt viteBuildProd
```

or

```shell
sbt viteBuildDev
```

This will generate a bundle which you can retrieve from:

```shell
target/scala-3.3.1/sbt-vite-[mode]/bundle
```

where `mode` is either `prod` or `dev`.

To run tests, simply run the usual task:

```shell
sbt test
```

This will bundle the compiled test code with all dependencies, so feel free to
include any dependencies in your unit tests! Just be sure you are using an 
appropriate `jsEnv` when testing code meant to run in the browser.

Finally, you can run a dev server by first generating a script:

```shell
sbt viteGenerateDevServerScript
```

and then running it from the project root:

```shell
./start-dev-server.sh
```

To hot-reload your Scala.js sources, execute the following sbt task in a separate
shell:

```shell
sbt "~vitePrepareDevSources"
```

This will detect updates to your Scala.js code, compile them, and inject them into the 
build environment so that the vite dev server will update.

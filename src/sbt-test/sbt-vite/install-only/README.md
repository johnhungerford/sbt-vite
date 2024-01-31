# Example: DependencyManagement.Manual

This project uses sbt-vite in "install-only" mode to build a complex multi-language
frontend project. Install-only means that sbt-vite will not manage your sources but will 
expect a `package.json` file 

The project is a calculator SPA built using React. Most of it is written in Scala.js,
but the button and display components are JavaScript, as is the top-level script that
mounts the top-level react component. The display component also imports a utility
exported from Scala.js. It thus demonstrates bidirectional interoperability between
Scala.js and JavaScript.

## Build structure

```
├── README.md
├── build.sbt
├── project
├── test
├── package.json
├── package-lock.json
├── index.html
├── main.jsx
├── node_modules
├── public
│       └── logo.png
└── src
     ├── main
     │     ├── javascript
     │     ├── scala
     │     └── styles
     └── test
           └── scala
```

Unlike the "managed" example, we need to keep all the usual web development
requirements in our project root: `package[-lock].json`, `node_modules`,
and the vite entry points `index.html`/`main.jsx` (along with their static
assets in `public`).

## Build settings

The only setting specific to sbt-vite is:

```sbt
viteDependencyManagement := DependencyManagement.Manual
```

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

All of these dependencies must be installed in the project root prior to building.

### Local dependencies

To import local dependencies into your Scala.js source code, the imported paths must be
relative to the sbt setting `viteProjectRoot`, which defaults to the project root:

```scala
// From src/main/scala/example/components/Display.scala
@js.native
@JSImport("/src/main/styles/buttonPanel.css", JSImport.Namespace)
object ButtonPanelCss extends js.Object

@js.native
@JSImport("/src/main/javascript/component/Button.jsx", JSImport.Default)
object ButtonRaw extends js.Object
```

### Scala.js dependencies

The above example shows how a Scala.js file can import a non-Scala.js, but what
about importing in the other direction? That is, how do import from Scala.js into
a JavaScript or TypeScript file?

This can be accomplished by prefixing imports with `scalajs:`. Vite will resolve this
as the output directory of `fullLinkJS` or `fastLinkJS` (depending on whether you are
using `viteBuildProd` or `viteBuildDev`). When there is nothing after the colon, it
is equivalent to `scalajs:main.js` (`main.js` is the main output of the Scala.js linker).

For instance, the entrypoint script in this project pulls the top level react app from
our Scala.js source as follows:

```javascript
// From main.jsx (project root)
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

Prior to building, we need to make sure all dependencies are in place. All of the 
dependencies need for our project are declared in `package.json`. Since this project 
is using "install-only" dependency management, sbt-vite will install these packages, 
along with any packages in the `npmDependencies` and `npmDevDependencies` sbt settings.

Notice, for instance, that unlike the "manual" example, `package.json` does not 
include any of the dev dependencies needed by sbt-vite. These are included by default in
`npmDevDependencies` and will be added automatically when you run build. Keep in mind 
that this will mutate `package.json`.

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

# sbt-vite

An SBT plugin to build and test Scala.js projects using vite.

## Usage

This plugin requires sbt 1.0.0+.

To use sbt-vite in your project, add the following line to `projects/plugins.sbt`:

```sbt
addSbtPlugin("io.github.johnhungerford.sbt.vite" % "sbt-vite" % "0.0.9")
```

In `build.sbt`, include `SbtVitePlugin` in `.enablePlugins(...)` in any Scala.js project 
that needs to be bundled with JavaScript dependencies, and configure the Scala.js plugin 
to use ECMAScript modules: 

```sbt
scalaJSLinkerConfig ~= {
	_.withModuleKind(ModuleKind.ESModule)
}
```

### Building

Add the following files to the root directory of your project or sub-project:

`index.html`
```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>sbt-vite</title>
  </head>
  <body>
    <script type="module" src="/main.js"></script>
  </body>
</html>
```

If your Scala.js project is runnable (i.e., if it has a `def main` entrypoint and
`scalaJSUseMainModuleInitializer := true` in build.sbt), you can simply import the 
application to be run as follows:

`main.js`:
```javascript
// 'scalajs:' will be resolved by vite to the output of the Scala.js linker
import 'scalajs:'
```

Otherwise you can simply import any exported objects from your Scala.js project as 
follows:

`main.js`:
```javascript
import { someLib, otherLib } from 'scalajs:';

...

someLib.doSomething();
otherLib.doSomethingElse();

...
```

Once you have your html and js entrypoints in place, you can run the following to 
generate a web bundle:

```shell
sbt viteBuildProd
```

This will compile your project, generate an appropriate vite configuration, and run 
vite on all artifacts. By default, the bundle will persisted at
`[project-directory]/target/scala-[x.x.x]/sbt-vite-prod/bundle`. Use `sbt viteBuildDev` 
to run build in development mode and skip optimizations.

To launch a development server, you can run:

```shell
sbt viteDevServer
```

or generate a script to launch a dev server without having to use the sbt console:

```shell
sbt viteGenerateDevServerScript
```

This will output a shell script `start-dev-server.sh` at your project root. It's 
recommended to use this script to launch the dev server rather than sbt so that you
can use your sbt console to run `~vitePrepareDevSources`. This will update your build
as you edit your Scala.js files so that vite can reload the page.

### Testing

Run tests using the usual command:

```shell
sbt test
```

This will use vite to bundle the linked JavaScript test executable with any dependencies
prior to running it.

## Dependency management

This plugin would not be of much use if it did not resolve dependencies properly. There 
are currently three modes of dependency management.

### Fully managed

By default, sbt-vite will manage all dependencies. To explicitly enable this 
mode, add the following setting to `build.sbt`:

```sbt
viteDependencyManagement := DependencyManagement.Managed(NpmManager.Npm)
```

You can also pass `NpmManager.Yarn` or `NpmManager.Pnpm` to use `yarn` or `pnpm` to 
install npm dependencies instead of `npm` (default).

You can declare npm dependencies to be used in your project using the following two 
sbt settings:

```sbt
npmDependencies ++= Seq(
    "react" -> "^18.2.0",
    "react-dom" -> "^18.2.0",
)

npmDevDependencies ++= Seq(
	"vite-plugin-eslint" -> "^1.8.1",
)
```

For `npmDevDependencies` in particular, be sure to use `++=` instead of `:=`, as 
sbt-vite includes several dependencies required to execute the build.

#### Other non-Scala.js sources

In addition to bundling npm modules, sbt-vite will bundle Scala.js outputs with 
imported sources, such as `JavaScript`, `TypeScript`, `css`, `less`, and others. 

Source files and directories can be declared for inclusion using the `viteOtherSources` 
setting:

```sbt
viteOtherSources := Seq(
    Location.FromProjectRoot(file("src/main/typescript")),
	Location.FromProjectRoot(file("src/main/styles")),
	Location.FromRoot(file("common/typescript")),
	Location.FromRoot(file("common/styles")),
	Location.FromRoot(file("common/index.html")),
	Location.FromRoot(file("common/main.js")),
)
```

(See [appendix below](#location))

For any declared source that points to a directory, sbt-vite will copy all the files 
and directories within it to the build directory prior to running `vite`. Any declared 
source that is a file will be copied directly to the build directory.

Accordingly, any sources declared in your build can be imported as expected:

```scala
// This will import either from [project]/src/main/typescript/someDir/someTypeScriptModule.ts
// or from common/typescript/someDir/someTypeScriptModule.ts
@js.native
@JSImport("/someDir/someTypeScriptModule", JSImport.Default)
object TypeScriptImport extends js.Object

// This will import either from [project]/src/main/styles/someStyle.css or from
// common/styles/someStyle.css
@js.native
@JSImport("/someStyle.css?inline", JSImport.Namespace)
object CssImport extends js.Object
```

These imports will work correctly in JS and TS sources as well:

```javascript
import someModule from '/someDir/someTypeScriptModule';
import '/someStyle.css';
```

### Manual

When dependency management is set to `Manual`, you will be responsible for managing any 
dependencies needed for vite to bundle your project. sbt-vite will neither install any
npm packages nor copy over any source directories. Instead, sbt-vite will simply run from 
`viteProjectRoot` (by default set to the root directory of your project or sub-project,
see [appendix below](#location)), from which it will expect to be able to resolve any 
imports, whether via local sources or `node_modules`.

Note that when using manual mode, `viteBuild` and `test` will fail unless you install
`vite`, `lodash`, `rollup-plugin-sourcemaps`, and `vite-plugin-scalajs` as dev 
dependencies. To have sbt-vite install these for you, use `InstallOnly` dependency 
management (see [below](#install-only)).

Note that sbt-vite currently supports vite versions only up to `4.5.2`. Vite 5 fails 
to resolve Scala.js outputs properly.

To enable manual dependency management, using the following setting in `build.sbt`:

```sbt
viteDependencyManagement := DependencyManagement.Manual
```

### Install-only

Install-only dependency management is like manual mode, only it will automatically
install any dependencies declared in `npmDependencies` and `npmDevDependencies` in 
your `viteProjectRoot` directory. This will ensure that any requirements needed simply 
to run the vite build commands will be in place, as these are included by default in 
`npmDevDependencies`.

## Customization

To allow users to customize the build process sbt-vite provides various settings for 
overriding configurations.

### Build command customization

Every build command executed by sbt-vite can be passed custom arguments or options as 
well as custom environment variables using the following settings:

```sbt
// Pass additional options or arguments to the "vite build" command
viteExtraArgs += "--mode=development"

// Set environment variables for the "vite build" command
viteEnvironment += "NODE_ENV" -> "development"

// Pass additional options or arguments to the "npm install" command
npmExtrArgs += "--legacy-peer-deps"

// Set environment variables for the "npm install" command
npmEnvironment += "NPM_CONFIG_PREFIX" -> "global_node_modules"

// Pass additional options or arguments to the "pnpm add" command
pnpmExtrArgs += "--save-exact"

// Set environment variables for the "pnpm add" command
pnpmEnvironment += "NPM_CONFIG_PREFIX" -> "global_node_modules"

// Pass additional options or arguments to the "yarn add" command
yarnExtrArgs += "--audit"

// Set environment variables for the "yarn add" command
yarnEnvironment += "NPM_CONFIG_PREFIX" -> "global_node_modules"
```

### Vite config overrides

sbt-vite generates configuration scripts with reasonable defaults for full builds
(i.e., `viteBuild`, which is an alias for `Compile / viteBuild`), and test builds
(i.e., `Test / viteBuild`, which prepares a bundle to be executed by `Test / test`).

To override these defaults, you can use the setting `viteConfigSources` to provide
one or more configuration scripts that will be merged with the defaults, allowing 
you to override various settings. Note that the following configuration properties
cannot be overridden:
1. `root`
2. `build.rollupOptions.input` (for tests only), and 
3. `build.rollupOptions.output.dir`

`viteConfigSources` must specify valid javascript files that provide a default export 
of one of the following two forms:
1. a simple [vite configuration object](https://github.com/vitejs/vite/blob/997a6951450640fed8cf19e58dce0d7a01b92392/packages/vite/src/node/config.ts#L127)
2. a function that consumes a [vite environment configuration](https://github.com/vitejs/vite/blob/997a6951450640fed8cf19e58dce0d7a01b92392/packages/vite/src/node/config.ts#L78)
   and returns a [vite configuration](https://github.com/vitejs/vite/blob/997a6951450640fed8cf19e58dce0d7a01b92392/packages/vite/src/node/config.ts#L127).

Note that neither of these should be wrapped in `defineConfig`, as this will be called 
after merging imported overrides.

Note also that the `viteConfigSources` will be merged in order, so later sources in the 
`Seq` will have precedence over prior sources.

`viteConfigSources` can be scoped to `Compile` and `Test` to provide different 
customizations for your full build and for tests.

#### Example

The following vite config source provides overrides to support JSX (React), 
bundle source maps (disabled by default except in tests), and break out several
library dependencies into separate chunks:

`build.sbt`:
```sbt
viteProdConfigSources += Location.FromRoot(file("vite.config-build.js"))
```

'vite.config-build.js':
```javascript
import react from '@vitejs/plugin-react';

import sourcemaps from 'rollup-plugin-sourcemaps';

export default (env)=> ({
  // Array properties will concat on merge, so this will be added
  // to plugins, instead of overwriting
  plugins: [
    react(), 
  ],
  build: {
    sourcemap: true,
    rollupOptions: {
      plugins: [sourcemaps()],
      output: {
        strict: false,
        chunkFileNames: '[name]-[hash:10].js',
        manualChunks: {
          lodash: ['lodash'],
          react: ['react'],
          'react-dom': ['react-dom'],
          'react-router-dom': ['react-router-dom'],
        }
      }
    },
  },
});
```

## Appendices

### Location

In order to identify directories and files more easily, sbt-vite uses a custom type 
`Location`, which allows you to specify paths relative to commonly used base directories:

1. `Location.Root`: the base directory of the root project
2. `Location.ProjectRoot`: the base directory of the currently scoped project
3. `Location.FromRoot(file)`: provide a `File` relative to `Location.Root`. Note
   that `file` must have a relative path.
4. `Location.FromProject(file)`: provide a `File` relative to `Location.ProjectRoot`.
   `file` must have a relative path.
5. `Location.FromCwd(file)`: provide a `File` relative to the current working directory
   (which will presumably always be the same as `Location.Root`). `file` need not 
   be relative, so use this to specify a location using an absolute path.

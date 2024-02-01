/**
 * Adapted from vite-plugin-scalajs under Apache 2.0 license
 * https://github.com/scala-js/vite-plugin-scalajs/blob/04a9f079cea2ab2570d4e49f8a2f41c14b1dda62/index.ts
 */

// @ts-ignore
import type { Plugin as VitePlugin } from "vite";

const scalaJsSourcePrefix: string = 'scalajs:';

export default function scalaJSPlugin(scalaJSOutputDir: string): VitePlugin {
    return {
        name: "sbt-vite:sbt-scalajs-plugin",

        // standard Rollup
        resolveId(source, importer, options) {
            if (source === scalaJsSourcePrefix) {
                return `${scalaJSOutputDir}/main.js`;
            } else if (source.startsWith(scalaJsSourcePrefix)) {
                const path = source.substring(scalaJsSourcePrefix.length);
                return `${scalaJSOutputDir}/${path}`
            } else {
                return null;
            }
        },
    };
}
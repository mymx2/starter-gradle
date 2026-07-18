import io.github.mymx2.plugin.spotless.defaultStep

plugins { id("io.github.mymx2.check.format-base") }

spotless {
  shell { defaultStep { shfmt() } }
  // prettier formatting is handled by io.github.mymx2.check.format-prettier (root project)
}

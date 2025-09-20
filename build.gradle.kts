plugins {
    id("com.falsepattern.fpgradle-mc") version "2.0.0"
}

group = "com.falsepattern"

minecraft_fp {
    mod {
        modid = "mcpatcher"
        name = "Right Proper MCPatcher"
        rootPkg = "$group.mcpatcher"
    }

    core {
        coreModClass = "internal.asm.CoreLoadingPlugin"
        accessTransformerFile = "mcpatcher_at.cfg"
    }

    mixin {
        pkg = "internal.mixin"
    }

    tokens {
        tokenClass = "Tags"
    }

    publish {
        maven {
            repoUrl = "https://mvn.falsepattern.com/releases"
            repoName = "mavenpattern"
        }
    }
}

tasks.processResources {
    from(file(".idea/icon.png")) {
        rename { "mcpatcher.png" }
    }
}

repositories {
    exclusive(mavenpattern(), "com.falsepattern")
    modrinthEX()
    cursemavenEX()
}

dependencies {
    apiSplit("com.falsepattern:falsepatternlib-mc1.7.10:1.9.0")
    compileOnly("it.unimi.dsi:fastutil:8.5.16")
    compileOnly("maven.modrinth:etfuturum:2.6.2:dev")
    compileOnly(deobfCurse("damage-indicators-mod-59489:2692129"))
}
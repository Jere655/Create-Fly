# Blocker de préparation NeoForge — 2026-10-16

La préparation NeoForge cible Minecraft `1.21.11` et NeoForge `21.11.42`.
Elle n'a pas atteint `javac` : les sources et le build Gradle se configurent,
mais NeoForm ne peut pas résoudre deux prérequis externes.

## Réseau Mojang

`libraries.minecraft.net` résout correctement (IPv4 `150.171.110.25`, IPv6
`2603:1061:14:113::1`) mais le handshake TLS expire aussi lorsque l'IPv4
officielle est forcée. Aucun proxy n'est configuré, l'horloge et le magasin
de certificats Java sont sains. En revanche, les endpoints officiels
`launchermeta.mojang.com`, `piston-data.mojang.com`,
`maven.neoforged.net` et Maven Central répondent.

Le seul dépôt trouvé pour `com.mojang:logging:1.1.1` est donc injoignable :
`https://libraries.minecraft.net/com/mojang/logging/1.1.1/logging-1.1.1.pom`.
Maven Central et les dépôts NeoForge releases/snapshots retournent `404` pour
cette coordonnée.

## Publication NeoForge incomplète

Le POM officiel de NeoForm
`net.neoforged:neoform:1.21.11-20251209.172050` dépend explicitement de
`net.neoforged:minecraft-dependencies:1.21.11`. Cette coordonnée retourne
`404` sur les dépôts officiels NeoForge releases et snapshots, y compris pour
`maven-metadata.xml`; elle n'est pas présente dans le cache Gradle réutilisé.

Ainsi, même après restauration de l'accès à `libraries.minecraft.net`, la
publication exacte NeoForge/NeoForm demandée reste incomplète tant que
`minecraft-dependencies:1.21.11` n'est pas publié (ou qu'une version
NeoForge cohérente n'est pas explicitement autorisée).

## Actions déjà validées

Gradle 9.5, Temurin 21 et ModDevGradle 2.0.107 configurent correctement le
projet et exposent `runClient` et `runServer`. Les outils de transformation
NeoForm ainsi que les bibliothèques publiques ont été résolus depuis Maven
Central et le dépôt NeoForge officiels, sans miroir tiers. Aucun fichier du
serveur-test n'a été modifié.

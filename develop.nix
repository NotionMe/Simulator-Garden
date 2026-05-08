{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = with pkgs; [
    maven
    jdk21
    sqlite
    # JavaFX dependencies for Wayland/X11
    gtk3
    glib
    libGL
    xorg.libXxf86vm
    xorg.libX11
    xorg.libXtst
    xorg.libXrender
  ];

  shellHook = ''
    # Set up library paths for JavaFX
    export LD_LIBRARY_PATH="${pkgs.lib.makeLibraryPath [
      pkgs.gtk3
      pkgs.glib
      pkgs.libGL
      pkgs.xorg.libXxf86vm
      pkgs.xorg.libX11
      pkgs.xorg.libXtst
      pkgs.xorg.libXrender
    ]}:$LD_LIBRARY_PATH"

    echo ""
    echo "╔═══════════════════════════════════════════════════════╗"
    echo "║                                                       ║"
    echo "║            🌱 GARDEN SIMULATOR 🌱                     ║"
    echo "║                                                       ║"
    echo "║         Development Environment v1.0.0                ║"
    echo "║                                                       ║"
    echo "╚═══════════════════════════════════════════════════════╝"
    echo ""
    echo "📦 Available tools:"
    echo "   • Maven $(mvn --version | head -n1 | cut -d' ' -f3)"
    echo "   • Java $(java -version 2>&1 | head -n1 | cut -d'"' -f2)"
    echo "   • SQLite $(sqlite3 --version | cut -d' ' -f1)"
    echo ""
    echo "🚀 Quick commands:"
    echo "   mvn compile              - Compile the project"
    echo "   mvn test                 - Run tests"
    echo "   mvn javafx:run           - Run JavaFX GUI"
    echo "   mvn flyway:migrate       - Run database migrations"
    echo "   mvn flyway:info          - Check migration status"
    echo "   mvn spotless:apply       - Format code"
    echo "   mvn clean package        - Build JAR"
    echo ""
    echo "📂 Project: Garden Simulator"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
  '';
}
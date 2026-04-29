{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = with pkgs; [
    maven
    jdk21
    sqlite
  ];

  shellHook = ''
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
name: release

jobs:
  release-windows:
    runs-on: windows-latest

    steps:
      - uses: actions/checkout@v2

      - uses: actions/setup-java@v2
        with:
          java-version: '11'
          distribution: 'adopt'
        
      - name: Validate Gradle wrapper
        uses: gradle/wrapper-validation-action@v1

      - name: Setup Gradle
        uses: gradle/gradle-build-action@v2

      - name: Execute Gradle build
        run: ./gradlew build

      - uses: "marvinpinto/action-automatic-releases@latest"
        with:
          repo_token: "${{ secrets.GITHUB_TOKEN }}"
          automatic_release_tag: "latest"
          prerelease: true
          title: "Development Build"
          files: |
            LICENSE.txt
            *.jar

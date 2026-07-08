package dev.ftb.mods.ftbquests.integration;

public interface DocsMod {
    void openDocsPage(String path);

    enum None implements DocsMod {
        INSTANCE;

        @Override
        public void openDocsPage(String path) {

        }
    }
}

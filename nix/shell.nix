{ pkgs, ... }:

pkgs.mkShell {
    name = "levenshtein";

    packages = with pkgs; [
        dotnet-sdk_7
        dotnet-runtime_7
        cowsay
    ];

    shellHook = ''
        clear
        cowsay Hello nerd! Ready to build Levenshtein!
    '';

    DOTNET_ROOT="${pkgs.dotnet-sdk_7}/share/dotnet";
}

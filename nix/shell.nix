{ pkgs, ... }:

pkgs.mkShell {
    name = "levenshtein";

    packages = with pkgs; [
        dotnet-sdk_8
        dotnet-runtime_8
        cowsay
    ];

    shellHook = ''
        clear
        cowsay Hello nerd! Ready to build Levenshtein!
    '';

    DOTNET_ROOT="${pkgs.dotnet-sdk_8}/share/dotnet";
}

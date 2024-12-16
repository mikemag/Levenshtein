{
    description = "Levenshtein build flake!";
    
    inputs = {
        nixpkgs.url = "github:nixos/nixpkgs/nixos-24.11-small";
    };

    outputs = { nixpkgs, ... }: 
    let
        pkgs = nixpkgs.legacyPackages."x86_64-linux";
    in 
    {
        devShells."x86_64-linux".default = import ./shell.nix { inherit pkgs; };
    };
}

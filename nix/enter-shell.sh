export NIXPKGS_ALLOW_INSECURE=1
nix develop ./nix --experimental-features 'nix-command flakes' --impure

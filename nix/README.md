# Nix

Among other things, [Nix](https://nixos.org/) package manager allows you to manage the dependencies of projects.

The build environment for this project has been provided as a Nix shell. To use it, first run:
`export NIXPKGS_ALLOW_INSECURE=1`
This is environment variable is required since dotnet 7 is considered "insecure" since it's no longer being maintained.
To enter the shell, run:
`nix develop ./nix --experimental-features 'nix-command flakes'`

And to build, run
`dotnet build`
in ../src.

And to test, run:
`dotnet run`
in ../src.

If you don't use Nix package manager, ignore this and just install the dotnet 7.0 sdk and runtime.

{
  pkgs ? import (fetchTarball {
    url = "https://github.com/NixOS/nixpkgs/archive/nixos-23.11.tar.gz";
    sha256 = "1f7d0blzwqcrvz94yj1whlnfibi5m6wzx0jqfn640xm5h9bwbm3r";
  }) { },
}:

let
  shell_packages = with pkgs; [
    openjdk17-bootstrap # Prebuilt
    maven
    bashInteractive
    maven
  ];
in
pkgs.mkShell {
  buildInputs = shell_packages;

  NIX_SHELL_PRESERVE_PROMPT = 1;
  shellHook = ''
    # Append "> nix-shell <" to PS1
    PROMPT_COMMAND='export PS1="$PS1\[\e[0;2;34m\]> nix-shell <\[\e[0m\] "; unset PROMPT_COMMAND'
  '';
}

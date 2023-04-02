#!/bin/bash

set -Eeuo pipefail
export LANG=C LC_ALL=C

handle_exit() {
    local -r -i exit_status="$?"
    echo "exiting with status $exit_status"
    exit "$exit_status"
}

trap handle_exit INT HUP TERM QUIT EXIT

process_args() {
    echo "${FUNCNAME[@]}"
    local -a routines=()
    while true; do
        local arg_val=''
        if [ $# -eq 0 ]; then
            break
        fi
        case "$1" in
            -h|--help)
                print_usage 0
                ;;
            --routine=*)
                arg_val="${1#*=}"
                ;&
            -r|--routine)
                if [ ! "$arg_val" ]; then
                    shift
                    [ "$#" -ge 1 ] && arg_val="$1"
                fi
                [ ! "$arg_val" ] && print_usage 1
                routines+=("$arg_val")
                ;;
        esac
        shift
    done
    for routine in "${routines[@]}"; do
        echo "got routine $routine"
    done
    for index in "${!routines[@]}"; do
        echo "got routine index $index and ${routines[$index]}"
    done
    for ((i=0; i<"${#routines[@]}"; i++)) do
        echo "got routine index $i and ${routines[$i]}"
    done
}
(process_args -r 001 --routine=002 --routine 003)

printfu() {
    echo "${FUNCNAME[@]}"
    local out=''
    printf -v out -- "-a my exec is %s" "$0"
    echo "$out"
}
(printfu)

process_kv() {
    echo "${FUNCNAME[@]}"
    head -5 /etc/passwd | while IFS=$':' read -r user pass uid _ name _; do
        echo "User: $user Pass: $pass UID: $uid Name: $name"
    done
}
(process_kv)

fd_io() {
    echo "${FUNCNAME[@]}"
    local -a lines=()
    exec 12< /etc/os-release
    IFS=$'\n' read -d $'\0' -r -a lines -u 12 || true
    echo "there are ${#lines[@]} lines, first line is ${lines[0]}"
    exec 12<&-

    exec 24< <(head -5 /etc/passwd)
    IFS=$'\n' read -d $'\0' -r -a lines -u 24 || true
    echo "there are ${#lines[@]} lines, first line is ${lines[0]}"
    exec 24<&-
}
(fd_io)

proc_subst() {
    echo "${FUNCNAME[@]}"
    local -i n=0
    while IFS=$':' read -r user pass uid _ name _; do
        echo "User: $user Pass: $pass UID: $uid Name: $name"
        (( n++ )) || true
    done < <(tail -n +5 /etc/passwd | head -n 5)
    echo "processed $n lines"

    while IFS=$'\0' read -d $'\0' -r path; do
        echo "Got path: $path"
    done < <(find / -print0 | head -z -n 5)
}
(proc_subst)

strings() {
    a='PRETTY_NAME="Ubuntu 22.04.2 LTS"'
    # Indexing
    echo "${b:-nostring}"
    echo "$a"
    echo "${a:12}"
    echo "${a:12:3}"
    echo "${#a}"
    # Basic manipulation
    echo "${a#PRETTY_NAME=}"
    echo "${a##PRETTY_NAME=}"
    echo "${a%LTS\"}"
    echo "${a%%LTS\"}"
    echo "${a/#PRETTY/DULL}"
    echo "${a/%LTS*/BETAHAHA}"
    echo "${a//LTS/BETA}"
    # Regex involvement
    [[ "$a" =~ .*LTS.* ]] && echo "yep is LTS"
    echo 'Regex exercises'
    echo "$(expr "$a" : '\(PRETTY\)' ) "
    echo "$(expr "$a" : '\(.*LTS.*\)' ) "
}
(strings)

symbol_ref_fun() {
    local -n symbol_ref_fun_ref=$1
    for key in "${!symbol_ref_fun_ref[@]}"; do
        val="${symbol_ref_fun_ref[$key]}"
        symbol_ref_fun_ref["$key"]="$(( val * 100 ))"
    done
}
symbol_ref() {
    local -A ary=(['a']=1 ['b']=2)
    symbol_ref_fun 'ary'
    for key in "${!ary[@]}"; do
        echo "Key: $key Value: ${ary[$key]}"
    done
}
(symbol_ref)

arrays() {
    local -a ary=('a1' 'a2')
    ary+=('a22' 'a222')
    for i in "${!ary[@]}"; do
        echo "Got index: $i"
    done
    unset 'ary[2]'
    for e in "${ary[@]}"; do
        if expr "$e" : '\(2\)'; then
            echo "Got element: $e"
        fi
    done
}
(arrays)



## Inventory of the binary files

Inventory of binary files PXF ships, after the RPM has been inflated

```bash
drwxr-xr-x.  121 .
├── drwxr-xr-x. 51 bin
│   ├── -rwxr-xr-x.     372 kill-pxf.sh
│   ├── -rwxr-xr-x.   17681 pxf
│   └── -rwxr-xr-x. 9860216 pxf-cli
├── -rw-r--r--. 41 commit.sha
├── drwxr-xr-x. 64 conf
│   ├── -rw-r--r--.  2613 pxf-env-default.sh
│   └── -rw-r--r--. 35361 pxf-profiles-default.xml
├── drwxr-xr-x. 30 gpextable
│   ├── drwxr-xr-x. 24 lib
│   │   └── drwxr-xr-x. 20 postgresql
│   │       └── -rwxr-xr-x. 563992 pxf.so
│   └── drwxr-xr-x. 24 share
│       └── drwxr-xr-x. 23 postgresql
│           └── drwxr-xr-x. 45 extension
│               ├── -rw-r--r--. 968 pxf--1.0.sql
│               └── -rw-r--r--. 192 pxf.control
├── drwxr-xr-x. 42 lib
│   └── -rw-r--r--. 29720 pxf-hbase-6.0.0-SNAPSHOT.jar
├── drwxr-xr-x. 44 server
│   └── -rw-r--r--. 103260746 pxf-service-6.0.0-SNAPSHOT.jar
├── drwxr-xr-x. 18 templates
│   └── drwxr-xr-x. 35 user
│       ├── drwxr-xr-x.  70 conf
│       │   ├── -rw-r--r--. 1449 pxf-env.sh
│       │   ├── -rw-r--r--. 2833 pxf-log4j2.xml
│       │   └── -rw-r--r--. 1178 pxf-profiles.xml
│       └── drwxr-xr-x. 278 templates
│           ├── -rw-r--r--.  759 adl-site.xml
│           ├── -rw-r--r--.  180 core-site.xml
│           ├── -rw-r--r--.  494 gs-site.xml
│           ├── -rw-r--r--.  295 hbase-site.xml
│           ├── -rw-r--r--.  712 hdfs-site.xml
│           ├── -rw-r--r--.  591 hive-site.xml
│           ├── -rw-r--r--. 5307 jdbc-site.xml
│           ├── -rw-r--r--.  310 mapred-site.xml
│           ├── -rw-r--r--.  617 minio-site.xml
│           ├── -rw-r--r--. 1464 pxf-site.xml
│           ├── -rw-r--r--.  407 s3-site.xml
│           ├── -rw-r--r--.  537 wasbs-site.xml
│           └── -rw-r--r--.  189 yarn-site.xml
└── -rw-r--r--. 15 version
```

## Make PXF read-only and try to init

We need to make sure the PXF binary directory `$PXF_HOME` is read-only and it
is not modified during `pxf init`.

```bash
sudo chmod -R -w $PXF_HOME
```

1. The first issue encountered with `pxf init`

    ```bash
     gpadmin@mdw  ~  PXF_CONF=/home/gpadmin/not-pxf pxf init
    Using /home/gpadmin/not-pxf as a location for user-configurable files
    sed: couldn't open temporary file /usr/local/pxf/conf/sedBGHlQq: Permission denied
    Directory /home/gpadmin/not-pxf already exists, no update required
    Directory /home/gpadmin/not-pxf/conf already exists, no update required
    Directory /home/gpadmin/not-pxf/keytabs already exists, no update required
    Directory /home/gpadmin/not-pxf/lib already exists, no update required
    Directory /home/gpadmin/not-pxf/servers already exists, no update required
    Directory /home/gpadmin/not-pxf/servers/default already exists, no update required
    Updating configurations from /usr/local/pxf/templates/user/templates to /home/gpadmin/not-pxf/templates ...
    Creating PXF runtime directory /usr/local/pxf/run ...
    mkdir: cannot create directory ‘/usr/local/pxf/run’: Permission denied
    chmod: cannot access '/usr/local/pxf/run': No such file or directory
    ```

    Two issues are noted here. The first issue is with `sed`, but that seems to
    succeed but it should probably fail. The second issue is with creating the
    `run` directory. A permission denied error is encountered.

    The `sed` issue, seems to be coming from this line:

    ```
     sed "${SED_OPTS[@]}" "s|{PXF_CONF:-.*}$|{PXF_CONF:-\"${PXF_CONF}\"}|g" "$default_env_script"
    ```

    This line of code is inside the `update_pxf_conf` line.

    The second issue is coming from creating the run dir. However, the run
    directory is configurable. To modify the `run` directory, we need to ask
    users to specify the `PXF_RUNDIR` environment variable during init.

2. The second issue encountered with `pxf init`

    Now that we know that `PXF_RUNDIR` can be set, we try to init again.
    
    ```
     gpadmin@mdw  ~  PXF_RUNDIR=~/run PXF_CONF=/home/gpadmin/not-pxf pxf init
    Using /home/gpadmin/not-pxf as a location for user-configurable files
    sed: couldn't open temporary file /usr/local/pxf/conf/sedG1E51w: Permission denied
    Directory /home/gpadmin/not-pxf already exists, no update required
    Directory /home/gpadmin/not-pxf/conf already exists, no update required
    Directory /home/gpadmin/not-pxf/keytabs already exists, no update required
    Directory /home/gpadmin/not-pxf/lib already exists, no update required
    Directory /home/gpadmin/not-pxf/servers already exists, no update required
    Directory /home/gpadmin/not-pxf/servers/default already exists, no update required
    Updating configurations from /usr/local/pxf/templates/user/templates to /home/gpadmin/not-pxf/templates ...
    Creating PXF runtime directory /usr/local/pxf/run ...
    mkdir: cannot create directory ‘/usr/local/pxf/run’: Permission denied
    chmod: cannot access '/usr/local/pxf/run': No such file or directory
    ```

    Unfortunately, this didn't work. We ignore the environment value during init.
    This can be fixed by accepting the environment variable in the
    `pxf-env-default.sh` file.

    ```
    # Path to Run directory
    export PXF_RUNDIR=${PXF_HOME}/run
    ```

    To solve this issue, we should either allow users to specify `PXF_RUNDIR`
    during init as illustrated above. As an alternative, we can create the `run`
    directory inside the `$PXF_CONF` directory. We allow setting a `PXF_RUNDIR`
    in `$PXF_CONF/conf/pxf-env.sh`, after PXF has already been inited, but it's
    not possible to specify the `PXF_RUNDIR` during init.

3. The third issue encountered

    After manually hacking `pxf init` to force it to create the `run` directory
    in a different location, another `sed` failure is encountered. The
    template files we copy from `$PXF_HOME` to `$PXF_CONF` are preserving the
    read-only flag, and therefore, we can't `sed` the `$PXF_CONF/conf/pxf-env.sh`
    file when trying to update the `JAVA_HOME` path during init.
    
    ```
    function editPxfEnvSh()
    {
        sed "${SED_OPTS[@]}" "s|.*JAVA_HOME=.*|JAVA_HOME=${JAVA_HOME}|g" "${PXF_CONF}/conf/pxf-env.sh"
    }
    ```

    To solve this issue, we need to `chmod` template files we copy from
    `$PXF_HOME` to `$PXF_CONF`.


In summary, the `pxf init` issues are as follows:

1. `sed` of the `pxf-env-default.sh` file
2. `run` directory is not picked up from environment variable
3. read-only permissions are propagated from templates to files that need `rw`
   privileges after init

Next, we try to run a `pxf start`

```bash
 gpadmin@mdw  ~/blah-pxf/conf  pxf start
ERROR: PXF is not yet initialized, you must run the 'pxf [cluster] init' command first.
```

As expected, PXF complains that pxf has not been initialized. This can be fixed
by allowing `PXF_CONF` to be set as an environment variable. Let's try that next:

```bash
 gpadmin@mdw  ~/blah-pxf/conf  PXF_CONF=/home/gpadmin/blah-pxf pxf start
ERROR: PXF is not yet initialized, you must run the 'pxf [cluster] init' command first.
```

Same error. This is probably an issue with all the pxf commands:

```bash
  gpadmin@mdw  ~/blah-pxf/conf  PXF_CONF=/home/gpadmin/blah-pxf pxf stop
 ERROR: PXF is not yet initialized, you must run the 'pxf [cluster] init' command first.
  gpadmin@mdw  ~/blah-pxf/conf  PXF_CONF=/home/gpadmin/blah-pxf pxf status
 ERROR: PXF is not yet initialized, you must run the 'pxf [cluster] init' command first.
  gpadmin@mdw  ~/blah-pxf/conf  PXF_CONF=/home/gpadmin/blah-pxf pxf restart
 ERROR: PXF is not yet initialized, you must run the 'pxf [cluster] init' command first.
  gpadmin@mdw  ~/blah-pxf/conf  PXF_CONF=/home/gpadmin/blah-pxf pxf register
  gpadmin@mdw  ~/blah-pxf/conf  PXF_CONF=/home/gpadmin/blah-pxf pxf version
 PXF version 6.0.0-SNAPSHOT
```

Only the `register` and `version` commands succeed.

The `start`, `stop`, `restart`, and  `status` commands should probably allow
execution if `PXF_CONF` is read from an environment variable.

## Follow symlinks when copying

When copying symlinks, we are not copying the original file, but rather, we
copy the symlink itself, and that is not a desired outcome for templates or
when registering the external table files into Greenplum.

These are the copy operations we have:

- `cp "${PXF_HOME}/templates/user/conf/pxf-log4j2.xml" "${PXF_CONF}/conf/pxf-log4j2.xml"`
- `setup_conf_directory() {...}`
- `cp -av ${parent_script_dir}/gpextable/* ${GPHOME}`
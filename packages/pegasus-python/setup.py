import os
import subprocess

from setuptools import find_namespace_packages, setup

src_dir = os.path.dirname(__file__)
home_dir = os.path.abspath(os.path.join(src_dir, "../.."))

install_requires = [
    # Utils
    # TODO: Replace attrs with the dataclasses module, when min Python version is >= 3.6
    "attrs",
    # 'dataclasses;python_version=="3.6"',
    # DAX/Workflow
    "PyYAML",
    # Python 2 compatibility
    "six>=1.9.0",
    # pegasus-init
    "Jinja2==2.10.3",
    "Flask-SQLAlchemy==2.3.2",
    "boto==2.48.0",
    "pamela==1.0.0",
    "globus-sdk==1.4.1",
    "pika==1.1.0",
    "Flask",
    "Flask-Caching",
    "requests",
    "sqlalchemy",
    "pegasus-wms.api",
    "pegasus-wms.dax",
    "pegasus-wms.common",
    "pegasus-wms.worker",
]

#
# Install conditional dependencies
#
def setup_installer_dependencies():
    global install_requires

    if subprocess.call(["which", "mysql_config"]) == 0:
        install_requires.append('MySQL-Python;python_version<="2.6"')
        install_requires.append('mysqlclient;python_version>"2.6"')


#
# Utility function to read the pegasus Version.in file
#
def read_version():
    return (
        subprocess.Popen(
            "%s/release-tools/getversion" % home_dir, stdout=subprocess.PIPE, shell=True
        )
        .communicate()[0]
        .decode()
        .strip()
    )


#
# Utility function to read the README file.
#
def read(fname):
    return open(os.path.join(src_dir, fname)).read()


def find_package_data(dirname):
    def find_paths(dirname):
        items = []
        for fname in os.listdir(dirname):
            path = os.path.join(dirname, fname)
            if os.path.isdir(path):
                items += find_paths(path)
            elif not path.endswith(".py") and not path.endswith(".pyc"):
                items.append(path)
        return items

    items = find_paths(dirname)
    return [path.replace(dirname, "") for path in items]


setup_installer_dependencies()

setup(
    name="pegasus-wms",
    version=read_version(),
    author="Pegasus Team",
    author_email="pegasus@isi.edu",
    description="Pegasus Workflow Management System Python API",
    long_description=read("README.md"),
    long_description_content_type="text/markdown",
    license="Apache2",
    url="http://pegasus.isi.edu",
    python_requires=">=3.5",
    keywords=["scientific workflows"],
    classifiers=[
        "Development Status :: 5 - Production/Stable",
        "Intended Audience :: Developers",
        "Intended Audience :: Science/Research",
        "Operating System :: Unix",
        "Programming Language :: Python",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.5",
        "Programming Language :: Python :: 3.6",
        "Programming Language :: Python :: 3.7",
        "Programming Language :: Python :: 3.8",
        "Topic :: Scientific/Engineering",
        "Topic :: Utilities",
        "License :: OSI Approved :: Apache Software License",
    ],
    namespace_packages=["Pegasus", "Pegasus.cli", "Pegasus.tools"],
    package_dir={"": "src"},
    packages=find_namespace_packages(where="src"),
    package_data={"Pegasus.service": find_package_data("src/Pegasus/service/")},
    include_package_data=True,
    zip_safe=False,
    install_requires=install_requires,
    extras_require={"postgresql": ["psycopg2"], "mysql": [], "cwl": ["cwl-utils"]},
)
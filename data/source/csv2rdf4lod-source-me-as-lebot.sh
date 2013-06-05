#3> <#> a <http://purl.org/twc/vocab/conversion/CSV2RDF4LOD_environment_variables> ;
#3>     rdfs:seeAlso 
#3>     <http://purl.org/twc/page/csv2rdf4lod/distributed_env_vars>,
#3>     <https://github.com/timrdf/csv2rdf4lod-automation/wiki/Script:-source-me.sh> .

export CSV2RDF4LOD_CONVERT_PERSON_URI="http://tw.rpi.edu/instances/TimLebo"
source /home/lebot/prizms/iogds/data/source/csv2rdf4lod-source-me-for-iogds.sh
alias iogds='sudo su iogds'
export PATH=$PATH`/home/lebot/opt/prizms/bin/install/paths.sh`
export CLASSPATH=$CLASSPATH`/home/lebot/opt/prizms/bin/install/classpaths.sh`
export CSV2RDF4LOD_HOME="/home/lebot/opt/prizms/repos/csv2rdf4lod-automation"

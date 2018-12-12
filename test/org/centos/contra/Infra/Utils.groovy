import spock.lang.*
import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

import org.centos.contra.Infra.Utils

import org.yaml.snakeyaml.Yaml

public class UtilsSpec extends JenkinsPipelineSpecification {
    @Shared
    def config = new HashMap<String,?>()

    @Shared
    def infraUtils
    def setup() {
        infraUtils = loadPipelineScriptForTest( "../../src/org/centos/contra/Infra/Utils.groovy" )
        infraUtils.getBinding().setVariable("env", ["foo": "bar"])
        explicitlyMockPipelineStep('dir')
        explicitlyMockPipelineStep('libraryResource')
        explicitlyMockPipelineStep('fileExists')
        explicitlyMockPipelineStep('writeFile')
    }
    
    def setupSpec() {
        Yaml yaml = new Yaml()
        config = (Map<String, ?>)yaml.load(new FileReader('test/contra-sample.yml'))
    }

    def "create aws instances" () {
        def aws = config.infra.provision.cloud.aws
        def i = 1
        when:
        def instances = infraUtils.createAwsInstances(aws as HashMap)

        then:
        instances != null
        // createAwsInstances modifies state so we need to modify it back
        def base_name = aws.instances[0].name
        if (aws.instances[0].count) {
            base_name = base_name[0..-3]
        }
        instances.every{ instance -> instance.getName() == base_name + "_" + i++ }
        instances.every{ instance -> instance.getRegion() == aws.instances[0].region }
        instances.every{ instance -> instance.getAmi() == aws.instances[0].ami }
        instances.every{ instance -> instance.getInstance_type() == aws.instances[0].instance_type }
        instances.every{ instance -> instance.getinstance_tags() == aws.instances[0].instance_tags }
        instances.every{ instance -> instance.getSecurity_groups() == aws.instances[0].security_groups.join(', ')}
        instances.every{ instance -> instance.getVpcSubnetID() == aws.instances[0].vpc_subnet_id}
        instances.every{ instance -> instance.getKeyPair() == aws.instances[0].key_pair}
        instances.every{ instance -> instance.getUser() == aws.instances[0].user}
        instances.every{ instance -> instance.getAssignPublicIP() == aws.instances[0].assign_public_ip}
    }

    def "creating aws instances drops to defaults when data is missing" () {
        def aws = [instances: [[:]]]
        when:
        def instances = infraUtils.createAwsInstances(aws as HashMap)

        then:
        instances != null
    }

    def "create beaker instances" () {
        def beaker = config.infra.provision.cloud.beaker
        when:
        def instances = infraUtils.createBeakerInstances(beaker as HashMap)

        then:
        instances != null
        // TODO: check instances for correctness
        instances.every { instance -> instances != null }
    }

    def "creating beaker instances drops to defaults when data is missing" () {
        def beaker = [instances: [[:]]]
        when:
        def instances = infraUtils.createBeakerInstances(beaker as HashMap)

        then:
        instances != null
    }

    def "create openstack instances" () {
        def openstack = config.infra.provision.cloud.openstack
        def len = openstack.instances.size()
        def i = 0
        when:
        def instances = infraUtils.createOpenstackInstances(openstack as HashMap)

        then:
        instances != null
        instances.every { instance -> instance.getNetwork() == openstack.network[0] }
        instances.every { instance -> instance.getKeyPair() == openstack.key_pair }
        instances.every { instance -> instance.getSecurityGroups() == openstack.security_groups.join(', ') }
        instances.every { instance -> instance.getName() == openstack.instances[i++].name }
        instances.every { instance -> instance.getFlavor() == openstack.instances[len-(i--)].flavor }
        instances.every { instance -> instance.getImage() == openstack.instances[i++].image }
        instances.every { instance -> instance.getFipPool() == openstack.instances[len-(i--)].floating_ip_pool }
        instances.every { instance -> instance.getUser() == openstack.instances[i++].user }
    }

    def "creating openstack instances drops to defaults when data is missing" () {
        def openstack = [instances: [[:]]]
        when:
        def instances = infraUtils.createOpenstackInstances(openstack as HashMap)

        then:
        instances != null
    }

    def "able to execute linchpin" () {
//        when:
//        infraUtils.executeInLinchpin("validate", "", true, "abc")
//
//        then:
//        noExceptionThrown()
    }

    def "handles failures in linchpin" () {
    }

    def "playbooks can be executed in the ansible container" () {
    }

    def "successfully create a topology string for each provider type" () {
        when:
        println("${instance.dump()}")
        print(config.infra.provision.cloud.beaker)
        infraUtils.generateTopology(instance, 1, "somedir")

        then:
        noExceptionThrown()

        where:
        instance << [
                infraUtils.createAwsInstances(config.infra.provision.cloud.aws as HashMap)[0],
                infraUtils.createBeakerInstances(config.infra.provision.cloud.beaker as HashMap)[0],
//                infraUtils.createOpenstackInstances(config.infra.provision.cloud.openstack as HashMap)[0]
        ]
    }

    def "correct topologies can be generated" () {
    }

    def "invalid topologies are handled" () {
    }
    
    def "key files can be generated based on provider type" () {
    }

    def "missing keyfiles are handled" () {
    }

    def "ssh keys can be generated for a given provider" () {
    }

    def "context files generated by linchpin can be parsed" () {
    }

    def "context file parsing errors are handled" () {
    }

    def "all instance context data is successfully acquired" () {
    }

    def "inventory file can be generate based on the results of a linchpin deployment" () {
    }

    def "get the template text from a resource file" () {
    }

    def "getTemplateText() handles non-matching provider parameter" () {

    }
}

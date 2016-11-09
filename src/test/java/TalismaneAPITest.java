import com.joliciel.talismane.TalismaneConfig;
import com.joliciel.talismane.TalismaneSession;
import com.joliciel.talismane.machineLearning.Decision;
import com.joliciel.talismane.parser.DependencyNode;
import com.joliciel.talismane.parser.ParseConfiguration;
import com.joliciel.talismane.parser.Parser;
import com.joliciel.talismane.posTagger.PosTagSequence;
import com.joliciel.talismane.posTagger.PosTaggedToken;
import com.joliciel.talismane.posTagger.PosTagger;
import com.joliciel.talismane.tokeniser.TokenSequence;
import com.joliciel.talismane.tokeniser.Tokeniser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.List;

/**
 * Created by dhouib on 14/10/2016.
 */
public class TalismaneAPITest {
    public static void main(String[] args) throws Exception {
      //  String text = "Le baume_Fermeté_sculputure, associant Skinfibrine et élastopeptides, vise à rendre la peau plus ferme et élastique.";
        String text = "La ligne s'anime également en mai avec une édition limitée Summer, rafraîchie d'ananas, création d'Ann Filpo et Catlos Benam";

        // arbitrary session id
        String sessionId = "";
        // load the Talismane configuration
        TalismaneSession talismaneSession;
        talismaneSession = TalismaneSession.getInstance(sessionId);
      /*  Config conf = ConfigFactory.load();
        TalismaneConfig talismaneConfig = new TalismaneConfig(conf, talismaneSession);*/
        Config parsedConfig =  ConfigFactory.parseFile(new File("src/main/resources/talismane-fr-3.0.0b.conf"));
        Config conf = ConfigFactory.load(parsedConfig);
        TalismaneConfig talismaneConfig = new TalismaneConfig(conf, talismaneSession);

        // tokenise the text
        Tokeniser tokeniser = talismaneConfig.getTokeniser();
        TokenSequence tokenSequence = tokeniser.tokeniseText(text);

        // pos-tag the token sequence
        PosTagger posTagger = talismaneConfig.getPosTagger();
        PosTagSequence posTagSequence = posTagger.tagSentence(tokenSequence);
        System.out.println("posTag: "+posTagSequence);
        Parser parser = talismaneConfig.getParser();
        ParseConfiguration parseConfiguration = parser.parseSentence(posTagSequence);

        for(int i=0; i<posTagSequence.size();i++){
            if(posTagSequence.get(i).getLexicalEntry()!=null) {

                //construire
                if(posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPP")){

                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for(PosTaggedToken ptt:pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                            List<PosTaggedToken> pos_tagged_token1 = parseConfiguration.getDependents(ptt);
                            for(PosTaggedToken ptt1:pos_tagged_token1) {
                                if (ptt1.getTag().toString().equalsIgnoreCase("P+D") && (parseConfiguration.getGoverningDependency(ptt1).getLabel().equalsIgnoreCase("dep")
                                        || parseConfiguration.getGoverningDependency(ptt1).getLabel().equalsIgnoreCase("mod"))) {
                                    List<PosTaggedToken> pos_tagged_token2 = parseConfiguration.getDependents(ptt1);
                                    for (PosTaggedToken ptt2 : pos_tagged_token2) {
                                        if (ptt2.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt2).getLabel().equalsIgnoreCase("prep")) {
                                            System.out.println("hasComponent: " + ptt2.getToken());
                                        }
                                    }
                                }
                                if (ptt1.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt1).getLabel().equalsIgnoreCase("coord")){
                                    List<PosTaggedToken> pos_tagged_token2 = parseConfiguration.getDependents(ptt1);
                                    for(PosTaggedToken ptt2:pos_tagged_token2) {
                                        if (ptt2.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt2).getLabel().equalsIgnoreCase("dep_coord")) {
                                            List<PosTaggedToken> pos_tagged_token3 = parseConfiguration.getDependents(ptt2);
                                            for (PosTaggedToken ptt3 : pos_tagged_token3) {
                                                if (ptt3.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt3).getLabel().equalsIgnoreCase("prep")) {
                                                    System.out.println("hasComponent: " + ptt3.getToken());
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }

                        if (ptt.getTag().toString().equalsIgnoreCase("P+D") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("mod")) {
                            List<PosTaggedToken> pos_tagged_token1 = parseConfiguration.getDependents(ptt);
                            for(PosTaggedToken ptt1:pos_tagged_token1) {

                                if (ptt1.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt1).getLabel().equalsIgnoreCase("prep")) {
                                    System.out.println("hasComponent: " + ptt1.getToken());
                                }

                                if (ptt1.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt1).getLabel().equalsIgnoreCase("coord")){
                                    List<PosTaggedToken> pos_tagged_token2 = parseConfiguration.getDependents(ptt1);
                                    for(PosTaggedToken ptt2:pos_tagged_token2) {
                                        if (ptt2.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt2).getLabel().equalsIgnoreCase("dep_coord")) {
                                            List<PosTaggedToken> pos_tagged_token3 = parseConfiguration.getDependents(ptt2);
                                            for (PosTaggedToken ptt3 : pos_tagged_token3) {
                                                if (ptt3.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt3).getLabel().equalsIgnoreCase("prep")) {
                                                    System.out.println("hasComponent: " + ptt3.getToken());
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }


                   if(posTagSequence.get(i).getLexicalEntry().getLemma().equalsIgnoreCase("reposer")) {
                    System.out.println("postag_iteration: " + posTagSequence.get(i).getLexicalEntry().getLemma());
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    System.out.println(pos_tagged_token);
                    for(PosTaggedToken ptt:pos_tagged_token){
                        if(ptt.getTag().toString().equalsIgnoreCase("NPP") &&parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")){
                            System.out.println("subject relation: "+ ptt.getToken());
                        }
                        // verifier si la liste de dépendence conitient un token sont le pos est NPP et le lien entre i et la dependance est de type "obj"
                        if(ptt.getTag().toString().equalsIgnoreCase("P")&&parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("p_obj")){
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            System.out.println(pos_tagged_token_test);
                            if(!pos_tagged_token_test.isEmpty()){
                                System.out.println(pos_tagged_token_test);
                                for(PosTaggedToken ptt_test:pos_tagged_token_test){
                                    if(ptt_test.getTag().toString().equalsIgnoreCase("NC") &&parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")){
                                        System.out.println("hasComponent: "+ ptt_test.getToken());
                                    }
                                }
                            }
                        }
                    }
                }

                 if(posTagSequence.get(i).getLexicalEntry().getLemma().equalsIgnoreCase("rafraîchir")) {
                       //Liste des dependences de i
                       List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                       System.out.println(pos_tagged_token);
                       for(PosTaggedToken ptt:pos_tagged_token){
                           // verifier si la liste de dépendence conitient un token sont le pos est NPP et le lien entre i et la dependance est de type "obj"
                           if(ptt.getTag().toString().equalsIgnoreCase("P")&&parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj")){
                               // liste de dependence
                               List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                               System.out.println(pos_tagged_token_test);
                               if(!pos_tagged_token_test.isEmpty()){
                                   System.out.println(pos_tagged_token_test);
                                   for(PosTaggedToken ptt_test:pos_tagged_token_test){
                                       if(ptt_test.getTag().toString().equalsIgnoreCase("NC")){
                                           System.out.println("hasComponent: "+ ptt_test.getToken());
                                       }
                                   }
                               }
                           }
                       }
                   }


                if(posTagSequence.get(i).getLexicalEntry().getLemma().equalsIgnoreCase("bouquet")) {
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    System.out.println("enter bouquet");
                    for (PosTaggedToken ptt : pos_tagged_token) {
                        if (ptt.getTag().toString().equalsIgnoreCase("P")&& parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("dep")) {
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            System.out.println(pos_tagged_token_test);
                            if (!pos_tagged_token_test.isEmpty()) {
                                System.out.println(pos_tagged_token_test);
                                for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                    if (ptt_test.getTag().toString().equalsIgnoreCase("NC")&& parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")) {
                                        System.out.println("hasComponent: " + ptt_test.getToken());
                                    }
                                    if (ptt_test.getTag().toString().equalsIgnoreCase("CC")&& parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("coord")) {
                                        List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                        for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                            if (ptt_test1.getTag().toString().equalsIgnoreCase("P")&& parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep_coord")) {
                                                List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                                for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                    if (ptt_test2.getTag().toString().equalsIgnoreCase("NC")&& parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")) {
                                                        System.out.println("hasComponent: " + ptt_test2.getToken());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }





                if(posTagSequence.get(i).getTag().toString().equalsIgnoreCase("V") ) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    System.out.println(pos_tagged_token);
                    for(PosTaggedToken ptt:pos_tagged_token){

                        if(ptt.getTag().toString().equalsIgnoreCase("NPP") &&parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")){
                            System.out.println("subject relation: "+ ptt.getToken());
                        }
                        // verifier si la liste de dépendence conitient un token sont le pos est NPP et le lien entre i et la dependance est de type "obj"
                        if((ptt.getTag().toString().equalsIgnoreCase("P")||ptt.getTag().toString().equalsIgnoreCase("P+D")) &&parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj") ) {
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            System.out.println(pos_tagged_token_test);
                            boolean is_annotated = false;
                            if(!pos_tagged_token_test.isEmpty()) {
                                System.out.println(pos_tagged_token_test);
                                for (PosTaggedToken ptt_test : pos_tagged_token_test) {
                                    if ((ptt_test.getTag().toString().equalsIgnoreCase("NPP")||ptt_test.getTag().toString().equalsIgnoreCase("NC"))&&parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")) {
                                        List<PosTaggedToken> pos_tagged_token_test_2 = parseConfiguration.getDependents(ptt_test);
                                        if(!pos_tagged_token_test_2.isEmpty())
                                        {for (PosTaggedToken ptt_test2 : pos_tagged_token_test_2) {
                                            if ((ptt_test2.getTag().toString().equalsIgnoreCase("ADJ")) && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("mod")) {
                                                System.out.println("hasComponent: " + ptt_test.getToken() + " " + ptt_test2.getToken());
                                                is_annotated=true;
                                            }
                                        }
                                            if(!is_annotated)
                                            {System.out.println("hasComponent: " + ptt_test.getToken());}
                                        }
                                        else {
                                            System.out.println("hasComponent: " + ptt_test.getToken());
                                        }

                                    }
                                    if(ptt_test.getTag().toString().equalsIgnoreCase("CC")&&parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("coord")){
                                        List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                        for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                            if (ptt_test1.getTag().toString().equalsIgnoreCase("P+D")&&parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep_coord")) {
                                                List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                                for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                    if (ptt_test2.getTag().toString().equalsIgnoreCase("NC")&&parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")) {
                                                        List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                                        if(!pos_tagged_token_test_3.isEmpty())
                                                        {for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                                            if ((ptt_test3.getTag().toString().equalsIgnoreCase("ADJ")) && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")) {
                                                                System.out.println("hasComponent: " + ptt_test2.getToken() + " " + ptt_test3.getToken());
                                                                is_annotated=true;
                                                            }
                                                        }
                                                            if(!is_annotated)
                                                            {System.out.println("hasComponent: " + ptt_test2.getToken());}
                                                        }
                                                        else {
                                                            System.out.println("hasComponent: " + ptt_test2.getToken());
                                                        }

                                                    }
                                                }
                                            }

                                            if (ptt_test1.getTag().toString().equalsIgnoreCase("P")&&parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep_coord")) {
                                                List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                                for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                    if (ptt_test2.getTag().toString().equalsIgnoreCase("NC")&&parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")) {
                                                        List<PosTaggedToken> pos_tagged_token_test_3 = parseConfiguration.getDependents(ptt_test2);
                                                        if(!pos_tagged_token_test_3.isEmpty())
                                                        {for (PosTaggedToken ptt_test3 : pos_tagged_token_test_3) {
                                                            if ((ptt_test3.getTag().toString().equalsIgnoreCase("ADJ")) && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")) {
                                                                System.out.println("hasComponent: " + ptt_test2.getToken() + " " + ptt_test3.getToken());
                                                                is_annotated=true;
                                                            }
                                                        }
                                                            if(!is_annotated)
                                                            {System.out.println("hasComponent: " + ptt_test2.getToken());}
                                                        }
                                                        else {
                                                            System.out.println("hasComponent: " + ptt_test2.getToken());
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                if(posTagSequence.get(i).getLexicalEntry().getLemma().equalsIgnoreCase("ajouter")) {
                     //Liste des dependences de i
                     List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                     System.out.println(pos_tagged_token);
                     for(PosTaggedToken ptt:pos_tagged_token){
                         // verifier si la liste de dépendence conitient un token sont le pos est NPP et le lien entre i et la dependance est de type "obj"
                         if(ptt.getTag().toString().equalsIgnoreCase("P")){
                             List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                             System.out.println(pos_tagged_token_test);
                             for(PosTaggedToken ptt_test:pos_tagged_token_test){
                                 if(ptt_test.getTag().toString().equalsIgnoreCase("NPP") &&parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")){
                                     System.out.println("subject relation: "+ ptt_test.getToken());
                                 }
                             }
                         }
                         if(ptt.getTag().toString().equalsIgnoreCase("CC")&&parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("coord")){
                             List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                             for(PosTaggedToken ptt_test:pos_tagged_token_test) {
                                 if (ptt_test.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("dep_coord")) {
                                     System.out.println("hasComponent: " + ptt_test.getToken());
                                 }
                             }
                         }
                         if(ptt.getTag().toString().equalsIgnoreCase("P+D")&&parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("de_obj")){
                             List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                             for(PosTaggedToken ptt_test:pos_tagged_token_test) {
                                 if (ptt_test.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test).getLabel().equalsIgnoreCase("prep")) {
                                     List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                     for (PosTaggedToken ptt_test1 : pos_tagged_token_test1) {
                                         if (ptt_test1.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test1).getLabel().equalsIgnoreCase("dep")) {
                                             System.out.println("hasComponent: " + ptt_test1.getToken());
                                             List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                             for (PosTaggedToken ptt_test2 : pos_tagged_token_test2) {
                                                 if (ptt_test2.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test2).getLabel().equalsIgnoreCase("prep")) {
                                                     List<PosTaggedToken> pos_tagged_token_test3 = parseConfiguration.getDependents(ptt_test2);
                                                     for (PosTaggedToken ptt_test3 : pos_tagged_token_test3) {
                                                         if (ptt_test3.getTag().toString().equalsIgnoreCase("ADJ") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("mod")) {
                                                             System.out.println("hasComponent: " + ptt_test2.getToken() + " " + ptt_test3.getToken());
                                                         }
                                                         if (ptt_test3.getTag().toString().equalsIgnoreCase("CC") && parseConfiguration.getGoverningDependency(ptt_test3).getLabel().equalsIgnoreCase("coord")) {
                                                             List<PosTaggedToken> pos_tagged_token_test4 = parseConfiguration.getDependents(ptt_test3);
                                                             for (PosTaggedToken ptt_test4 : pos_tagged_token_test4) {
                                                                 if (ptt_test4.getTag().toString().equalsIgnoreCase("NC") && parseConfiguration.getGoverningDependency(ptt_test4).getLabel().equalsIgnoreCase("dep_coord")) {
                                                                     List<PosTaggedToken> pos_tagged_token_test5 = parseConfiguration.getDependents(ptt_test4);
                                                                     for (PosTaggedToken ptt_test5 : pos_tagged_token_test5) {
                                                                         if (ptt_test5.getTag().toString().equalsIgnoreCase("P") && parseConfiguration.getGoverningDependency(ptt_test5).getLabel().equalsIgnoreCase("dep")) {
                                                                             List<PosTaggedToken> pos_tagged_token_test6 = parseConfiguration.getDependents(ptt_test5);
                                                                             for (PosTaggedToken ptt_test6 : pos_tagged_token_test6) {
                                                                                 if (ptt_test6.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test6).getLabel().equalsIgnoreCase("Prep")) {
                                                                                     List<PosTaggedToken> pos_tagged_token_test7 = parseConfiguration.getDependents(ptt_test6);
                                                                                     for (PosTaggedToken ptt_test7 : pos_tagged_token_test7) {
                                                                                         if (ptt_test7.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt_test7).getLabel().equalsIgnoreCase("mod")) {
                                                                                             System.out.println("hasComponent: " + ptt_test6.getToken() + " " + ptt_test7.getToken());
                                                                                         }
                                                                                     }
                                                                                 }
                                                                             }
                                                                         }
                                                                     }
                                                                 }
                                                             }
                                                         }
                                                     }
                                                 }
                                             }
                                         }
                                     }

                                 }
                             }
                         }

                     }
                 }

                if(posTagSequence.get(i).getTag().toString().equalsIgnoreCase("VPR") ) {
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    System.out.println(pos_tagged_token);
                    for(PosTaggedToken ptt:pos_tagged_token){
                        // verifier si la liste de dépendence conitient un token sont le pos est NPP et le lien entre i et la dependance est de type "obj"
                        if(ptt.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj") ){
                            System.out.println("hasComponent: "+ ptt.getToken());
                           // liste de dependence
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            if(!pos_tagged_token_test.isEmpty()){
                                System.out.println(pos_tagged_token_test);
                                for(PosTaggedToken ptt_test:pos_tagged_token_test){
                                    if(ptt_test.getTag().toString().equalsIgnoreCase("CC")){
                                        List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                        for(PosTaggedToken ptt_test1:pos_tagged_token_test1 ){
                                            System.out.println("hasComponent: "+ ptt_test1.getToken());
                                        }
                                    }
                                    else if(ptt_test.getTag().toString().equalsIgnoreCase("NPP")){
                                        System.out.println("hasComponent: "+ ptt_test.getToken());
                                        List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                        if(!pos_tagged_token_test1.isEmpty()){
                                            for(PosTaggedToken ptt_test1:pos_tagged_token_test1 ){
                                                if(ptt_test1.getTag().toString().equalsIgnoreCase("CC")){
                                                    List<PosTaggedToken> pos_tagged_token_test2 = parseConfiguration.getDependents(ptt_test1);
                                                    for(PosTaggedToken ptt_test2:pos_tagged_token_test2 ){
                                                        System.out.println("hasComponent: "+ ptt_test2.getToken());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                else   if(posTagSequence.get(i).getLexicalEntry().getLemma().equalsIgnoreCase("caractériser")) {
                    System.out.println("postag_iteration: " + posTagSequence.get(i).getLexicalEntry().getLemma());
                    //Liste des dependences de i
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    System.out.println(pos_tagged_token);
                    for(PosTaggedToken ptt:pos_tagged_token){
                        // verifier si la liste de dépendence conitient un token sont le pos est NPP et le lien entre i et la dependance est de type "obj"
                        if(ptt.getTag().toString().equalsIgnoreCase("P")){
                            // liste de dependence
                            List<PosTaggedToken> pos_tagged_token_test = parseConfiguration.getDependents(ptt);
                            System.out.println(pos_tagged_token_test);
                            if(!pos_tagged_token_test.isEmpty()){
                                System.out.println(pos_tagged_token_test);
                                for(PosTaggedToken ptt_test:pos_tagged_token_test){
                                    if(ptt_test.getTag().toString().equalsIgnoreCase("NC")){
                                        System.out.println("hasComponent: "+ ptt_test.getToken());
                                        List<PosTaggedToken> pos_tagged_token_test1 = parseConfiguration.getDependents(ptt_test);
                                        System.out.println(pos_tagged_token_test1);
                                        for(PosTaggedToken ptt_test1:pos_tagged_token_test1){
                                            if(ptt_test1.getTag().toString().equalsIgnoreCase("NC")){
                                                System.out.println("hasComponent: "+ ptt_test1.getToken());
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                }

                if(posTagSequence.get(i).getLexicalEntry().getLemma().equalsIgnoreCase("incarner")){
                    System.out.println("deppppProjective: "+parseConfiguration.getNonProjectiveDependencies());
                    List<PosTaggedToken> pos_tagged_token = parseConfiguration.getDependents(posTagSequence.get(i));
                    for(PosTaggedToken ptt:pos_tagged_token){
                        if(ptt.getTag().toString().equalsIgnoreCase("NPP") &&parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("suj")){
                            List<PosTaggedToken> pos_tagged_token1 = parseConfiguration.getDependents(ptt);
                            for(PosTaggedToken ptt1:pos_tagged_token1) {
                                if (ptt1.getTag().toString().equalsIgnoreCase("NPP") && parseConfiguration.getGoverningDependency(ptt1).getLabel().equalsIgnoreCase("mod")) {
                                    System.out.println("hasAmbassador: " + ptt.getToken() + " "+ptt1.getToken());
                                }
                            }
                        }

                        else if(parseConfiguration.getGoverningDependency(ptt).getLabel().equalsIgnoreCase("obj")){
                            System.out.println("subject relation: "+ ptt.getToken());

                        }
                    }

                }


            }
          }

        DependencyNode dependencyNode = parseConfiguration.getParseTree();
        System.out.println(dependencyNode);
       /* List<PosTaggedToken> test_dep = parseConfiguration.getDependents(postag);
        System.out.println("right: " +parseConfiguration.getRightDependents(postag));
        System.out.println("test_dep: "+test_dep);
        System.out.println("dep: "+dependencyNode);
        System.out.println(dependencyNode.getLastToken().getPosTaggedToken());*/

     //   System.out.println(dependencyNode.getAllNodes(););
    }

}

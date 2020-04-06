extern crate toml;
extern crate serde;
extern crate serde_derive;
extern crate threshold_secret_sharing as tss;

use serde_derive::{Deserialize, Serialize};

#[derive(Deserialize, Serialize, Debug)]
pub struct Config {
    name: String,
    num_bins: usize,
    name_bins: Vec<String>,
    uuid: String,
    parties: Vec<Party>,
    shares: Vec<Share>,
}
#[derive(Deserialize, Serialize, Debug)]
pub struct Party {
    name: String,
    email: String,
    id: u8,
}

#[derive(Deserialize, Serialize, Debug)]
pub struct Share {
    share: i64,
    party: u8,
    bin: usize,
}

pub fn test() {
    println!("works!");
}

fn collect_shares(shares: &[Share], bin_num: usize) -> Vec<i64>{
    shares.iter().filter_map(|s| if s.bin == bin_num {Some(s.share)} else {None}).collect()
}
pub fn sum_shares(shares: &[Share], bin_num:usize, prime: i64) -> i64{
    let test: Vec<i64> = shares.iter().filter_map(|s| if s.bin == bin_num {Some(s.share)} else {None}).collect();
    println!("Collects with prime {}: {:#?}",prime, test);
    test.iter().fold(0i64, |acc, s| (acc + s) % prime)
}
pub fn share_test() {
        let ref tss = tss::shamir::ShamirSecretSharing {
            threshold: 1,           // privacy threshold
            share_count: 2,        // total number of shares to generate
            prime: 2^(127)-1              // prime field to use
            };
        let indices = vec![0_usize, 1_usize];
        let secret1 = 5;
        let secret2 = 3;
        let ssecret1 = tss.share(secret1);
        let ssecret2 = tss.share(secret2);
        let sum: Vec<i64> = ssecret1.iter().zip(ssecret2.clone()).map(|(a,b)| (a+b)& tss.prime).collect();
        let secrets1 = vec![Share{share: ssecret1[0], party: 0,bin: 0},
                           Share{share: ssecret2[1], party: 1, bin: 0}];
        let secrets2 = vec![Share{share: ssecret1[1], party: 0, bin: 0},
                           Share{share: ssecret2[0], party: 1, bin: 0}];
        let sum1 = sum_shares(&secrets1,0, tss.prime);
        let sum2 = sum_shares(&secrets2,0, tss.prime);
        let sumsec = vec![sum1, sum2];
        println!("ssecret1: {:#?}", ssecret1);
        println!("ssecret2: {:#?}", ssecret2);
        println!("View Party1: {:#?}", secrets1);
        println!("View Party2: {:#?}", secrets2);
        println!("Sum1: {}", sum1);
        println!("Sum2: {}", sum2);
        println!("Sum_direct: {:#?}", sum);
        println!("sumshares: {:#?}", sumsec);
        let answer = tss.reconstruct(&indices, &sumsec);
        assert!(answer == 8, "Answer is {}", answer);
    }

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn toml_test() {
        let conf: Config = toml::from_str(r#"
        name = "Sex Distribution of CBS Group"
        num_bins = 2
        name_bins = ["male", "female"]
        uuid = "1234abcd"

        [[parties]]
        name = "Party 1"
        email = "party1@party1.de"
        id = 0

        [[parties]]
        name = "Party 2"
        email = "party2@party2.de"
        id = 1

        [[shares]]
        share = 1232341234235
        party = 0
        bin = 0


        [[shares]]
        share = 2345
        party = 0
        bin =1
        "#).unwrap();

        println!("{:#?}", conf);
    }
}

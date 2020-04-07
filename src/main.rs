mod secret;
mod state;

fn main(){
    let mut model = state::ProgramModel::new();
    model.add_bin("Testbin");
    model.add_participant("Test Participant", "test@test.com");
    let arith = secret::ArithmeticSharing::new(5);
    let arith_secret = secret::ValueType::ArithmeticValue(7);
    let arith_shares = arith.share(arith_secret);
    println!("Arithmetic Sharing:");
    println!("Shares: {:#?}", arith_shares);
    println!("Reconstructed: {:#?}, should be {:#?}", arith.reconstruct(&arith_shares), arith_secret);
    let shamir = secret::ShamirsSharing::new(5);
    let shamir_secret = secret::ValueType::ShamirValue(15);
    let shamir_shares = shamir.share(shamir_secret);
    println!("Shamir's Sharing:");
    println!("Shares: {:#?}", shamir_shares);
    println!("Reconstructed: {:#?}, should be {:#?}", shamir.reconstruct(&shamir_shares), shamir_secret);
}
